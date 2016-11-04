/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;
import javax.annotation.*;

public final class GenericTypeReflection
{
   @Nonnull private final Map<String, Type> typeParametersToTypeArguments;
   @Nonnull private final Map<String, String> typeParametersToTypeArgumentNames;
   @Nonnull private final Class<?> ownerType;
   private final boolean withSignatures;

   public GenericTypeReflection(@Nonnull Class<?> ownerClass, @Nullable Type genericType)
   {
      this(ownerClass, genericType, true);
   }

   public GenericTypeReflection(@Nonnull Class<?> ownerClass, @Nullable Type genericType, boolean withSignatures)
   {
      typeParametersToTypeArguments = new HashMap<String, Type>(4);
      typeParametersToTypeArgumentNames =
         withSignatures ? new HashMap<String, String>(4) : Collections.<String, String>emptyMap();
      ownerType = ownerClass;
      this.withSignatures = withSignatures;
      discoverTypeMappings(ownerClass, genericType);
   }

   private void discoverTypeMappings(@Nonnull Class<?> rawType, @Nullable Type genericType)
   {
      if (genericType instanceof ParameterizedType) {
         addMappingsFromTypeParametersToTypeArguments(rawType, (ParameterizedType) genericType);
      }

      addGenericTypeMappingsForSuperTypes(rawType);
   }

   private void addGenericTypeMappingsForSuperTypes(@Nonnull Class<?> rawType)
   {
      Type superType = rawType;

      while (superType instanceof Class<?> && superType != Object.class) {
         Class<?> superClass = (Class<?>) superType;
         superType = superClass.getGenericSuperclass();

         if (superType != null && superType != Object.class) {
            superClass = addGenericTypeMappingsIfParameterized(superType);
            superType = superClass;
         }

         addGenericTypeMappingsForInterfaces(superClass);
      }
   }

   @Nonnull
   private Class<?> addGenericTypeMappingsIfParameterized(@Nonnull Type superType)
   {
      if (superType instanceof ParameterizedType) {
         ParameterizedType genericSuperType = (ParameterizedType) superType;
         Class<?> rawType = (Class<?>) genericSuperType.getRawType();
         addMappingsFromTypeParametersToTypeArguments(rawType, genericSuperType);
         return rawType;
      }

      return (Class<?>) superType;
   }

   private void addGenericTypeMappingsForInterfaces(@Nonnull Class<?> classOrInterface)
   {
      for (Type implementedInterface : classOrInterface.getGenericInterfaces()) {
         Class<?> implementedType = addGenericTypeMappingsIfParameterized(implementedInterface);
         addGenericTypeMappingsForInterfaces(implementedType);
      }
   }

   @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
   private void addMappingsFromTypeParametersToTypeArguments(
      @Nonnull Class<?> rawType, @Nonnull ParameterizedType genericType)
   {
      String ownerTypeDesc = getOwnerClassDesc(rawType);
      TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
      Type[] typeArguments = genericType.getActualTypeArguments();
      int n = typeParameters.length;

      for (int i = 0; i < n; i++) {
         TypeVariable<?> typeParam = typeParameters[i];
         String typeVarName = typeParam.getName();

         if (typeParametersToTypeArguments.containsKey(ownerTypeDesc + ':' + typeVarName)) {
            continue;
         }

         Type typeArg = typeArguments[i];
         Type mappedTypeArg;
         String mappedTypeArgName = null;

         if (typeArg instanceof Class<?>) {
            mappedTypeArg = typeArg;

            if (withSignatures) {
               mappedTypeArgName = 'L' + getOwnerClassDesc((Class<?>) typeArg);
            }
         }
         else if (typeArg instanceof TypeVariable<?>) {
            mappedTypeArg = typeArg;

            if (withSignatures) {
               TypeVariable<?> typeVar = (TypeVariable<?>) typeArg;
               String ownerClassDesc = getOwnerClassDesc(typeVar);
               String intermediateTypeArg = ownerClassDesc + ":T" + typeVar.getName();
               mappedTypeArgName = typeParametersToTypeArgumentNames.get(intermediateTypeArg);
            }
         }
         else if (typeArg instanceof ParameterizedType) {
            mappedTypeArg = typeArg;

            if (withSignatures) {
               Class<?> classType = getClassType(typeArg);
               mappedTypeArgName = 'L' + getOwnerClassDesc(classType);
            }
         }
         else if (typeArg instanceof GenericArrayType) {
            mappedTypeArg = typeArg;

            if (withSignatures) {
               Type componentType = ((GenericArrayType) typeArg).getGenericComponentType();
               Class<?> classType = getClassType(componentType);
               mappedTypeArgName = "[L" + getOwnerClassDesc(classType);
            }
         }
         else {
            mappedTypeArg = typeParam.getBounds()[0];

            if (withSignatures) {
               Class<?> classType = getClassType(mappedTypeArg);
               mappedTypeArgName = 'L' + getOwnerClassDesc(classType);
            }
         }

         addTypeMapping(ownerTypeDesc, typeVarName, mappedTypeArg, mappedTypeArgName);
      }
   }

   @Nonnull
   private String getOwnerClassDesc(@Nonnull Class<?> rawType) { return rawType.getName().replace('.', '/'); }

   @Nonnull
   private Class<?> getClassType(@Nonnull Type type)
   {
      if (type instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         return (Class<?>) parameterizedType.getRawType();
      }

      if (type instanceof TypeVariable<?>) {
         TypeVariable<?> typeVar = (TypeVariable<?>) type;
         String typeVarKey = getTypeVariableKey(typeVar);
         Type typeArg = typeParametersToTypeArguments.get(typeVarKey);

         if (typeArg == null) {
            throw new IllegalArgumentException("Unable to resolve type variable \"" + typeVar.getName() + '"');
         }

         //noinspection TailRecursion
         return getClassType(typeArg);
      }

      return (Class<?>) type;
   }

   private void addTypeMapping(
      @Nonnull String ownerTypeDesc, @Nonnull String typeVarName,
      @Nonnull Type mappedTypeArg, @Nullable String mappedTypeArgName)
   {
      typeParametersToTypeArguments.put(ownerTypeDesc + ':' + typeVarName, mappedTypeArg);

      if (mappedTypeArgName != null) {
         addTypeMapping(ownerTypeDesc, typeVarName, mappedTypeArgName);
      }
   }

   private void addTypeMapping(
      @Nonnull String ownerTypeDesc, @Nonnull String typeVarName, @Nonnull String mappedTypeArgName)
   {
      String typeMappingKey = ownerTypeDesc + ":T" + typeVarName;
      typeParametersToTypeArgumentNames.put(typeMappingKey, mappedTypeArgName);
   }

   public final class GenericSignature
   {
      private final List<String> parameters;
      private final String parameterTypeDescs;
      private final int lengthOfParameterTypeDescs;
      private int currentPos;

      GenericSignature(@Nonnull String signature)
      {
         int p = signature.indexOf('(');
         int q = signature.lastIndexOf(')');
         parameterTypeDescs = signature.substring(p + 1, q);
         lengthOfParameterTypeDescs = parameterTypeDescs.length();
         parameters = new ArrayList<String>();
         addTypeDescsToList();
      }

      private void addTypeDescsToList()
      {
         while (currentPos < lengthOfParameterTypeDescs) {
            addNextParameter();
         }
      }

      private void addNextParameter()
      {
         int startPos = currentPos;
         int endPos;
         char c = parameterTypeDescs.charAt(startPos);

         if (c == 'T') {
            endPos = parameterTypeDescs.indexOf(';', startPos);
            currentPos = endPos;
         }
         else if (c == 'L') {
            endPos = advanceToEndOfTypeDesc();
         }
         else if (c == '[') {
            char elemTypeStart = firstCharacterOfArrayElementType();

            if (elemTypeStart == 'T') {
               endPos = parameterTypeDescs.indexOf(';', startPos);
               currentPos = endPos;
            }
            else if (elemTypeStart == 'L') {
               endPos = advanceToEndOfTypeDesc();
            }
            else {
               endPos = currentPos + 1;
            }
         }
         else {
            endPos = currentPos + 1;
         }

         currentPos++;
         String parameter = parameterTypeDescs.substring(startPos, endPos);
         parameters.add(parameter);
      }

      private int advanceToEndOfTypeDesc()
      {
         char c = '\0';

         do {
            currentPos++;
            if (currentPos == lengthOfParameterTypeDescs) break;
            c = parameterTypeDescs.charAt(currentPos);
         } while (c != ';' && c != '<');

         int endPos = currentPos;

         if (c == '<') {
            advancePastTypeArguments();
            currentPos++;
         }

         return endPos;
      }

      private char firstCharacterOfArrayElementType()
      {
         char c;

         do {
            currentPos++;
            c = parameterTypeDescs.charAt(currentPos);
         } while (c == '[');

         return c;
      }

      private void advancePastTypeArguments()
      {
         int angleBracketDepth = 1;

         do {
            currentPos++;
            char c = parameterTypeDescs.charAt(currentPos);
            if (c == '>') angleBracketDepth--; else if (c == '<') angleBracketDepth++;
         } while (angleBracketDepth > 0);
      }

      public boolean satisfiesGenericSignature(@Nonnull String otherSignature)
      {
         GenericSignature other = new GenericSignature(otherSignature);
         return areMatchingSignatures(other);
      }

      private boolean areMatchingSignatures(@Nonnull GenericSignature other)
      {
         int n = parameters.size();

         if (n != other.parameters.size()) {
            return false;
         }

         for (int i = 0; i < n; i++) {
            String p1 = other.parameters.get(i);
            String p2 = parameters.get(i);

            if (!areParametersOfSameType(p1, p2)) {
               return false;
            }
         }

         return true;
      }

      @SuppressWarnings("MethodWithMultipleLoops")
      private boolean areParametersOfSameType(@Nonnull String param1, @Nonnull String param2)
      {
         if (param1.equals(param2)) return true;

         int i = -1;
         char c;
         do { i++; c = param1.charAt(i); } while (c == '[');
         if (c != 'T') return false;

         String typeVarName1 = param1.substring(i);
         String typeVarName2 = param2.substring(i);
         String typeArg1 = null;

         for (Entry<String, String> typeParamAndArgName : typeParametersToTypeArgumentNames.entrySet()) {
            String typeMappingKey = typeParamAndArgName.getKey();
            String typeVarName = typeMappingKey.substring(typeMappingKey.indexOf(':') + 1);

            if (typeVarName.equals(typeVarName1)) {
               typeArg1 = typeParamAndArgName.getValue();
               break;
            }
         }

         return typeVarName2.equals(typeArg1);
      }

      public boolean satisfiesSignature(@Nonnull String otherSignature)
      {
         GenericSignature other = new GenericSignature(otherSignature);
         return other.areMatchingSignatures(this);
      }
   }

   @Nonnull
   public GenericSignature parseSignature(@Nonnull String genericSignature)
   {
      return new GenericSignature(genericSignature);
   }

   @Nonnull
   public String resolveReturnType(@Nonnull String ownerTypeDesc, @Nonnull String genericSignature)
   {
      addTypeArgumentsIfAvailable(ownerTypeDesc, genericSignature);

      int p = genericSignature.lastIndexOf(')') + 1;
      int q = genericSignature.length();
      String returnType = genericSignature.substring(p, q);
      String resolvedReturnType = replaceTypeParametersWithActualTypes(ownerTypeDesc, returnType);

      StringBuilder finalSignature = new StringBuilder(genericSignature);
      finalSignature.replace(p, q, resolvedReturnType);
      return finalSignature.toString();
   }

   private void addTypeArgumentsIfAvailable(@Nonnull String ownerTypeDesc, @Nonnull String signature)
   {
      int firstParen = signature.indexOf('(');
      if (firstParen == 0) return;

      int p = 1;
      boolean lastMappingFound = false;

      while (!lastMappingFound) {
         int q = signature.indexOf(':', p);
         String typeVar = signature.substring(p, q);

         q++;

         if (signature.charAt(q) == ':') {
            q++; // an unbounded type argument uses ":" as separator, while a bounded one uses "::"
         }

         int r = signature.indexOf(':', q);

         if (r < 0) {
            r = firstParen - 2;
            lastMappingFound = true;
         }
         else {
            r = signature.lastIndexOf(';', r);
            p = r + 1;
         }

         String typeArg = signature.substring(q, r);
         addTypeMapping(ownerTypeDesc, typeVar, typeArg);
      }
   }

   @Nonnull
   private String replaceTypeParametersWithActualTypes(@Nonnull String ownerTypeDesc, @Nonnull String typeDesc)
   {
      if (typeDesc.charAt(0) == 'T' && !typeParametersToTypeArgumentNames.isEmpty()) {
         String typeParameter = typeDesc.substring(0, typeDesc.length() - 1);
         String typeArg = typeParametersToTypeArgumentNames.get(ownerTypeDesc + ':' + typeParameter);
         return typeArg == null ? typeDesc : typeArg + ';';
      }

      int p = typeDesc.indexOf('<');

      if (p < 0) {
         return typeDesc;
      }

      String resolvedTypeDesc = typeDesc;

      for (Entry<String, String> paramAndArg : typeParametersToTypeArgumentNames.entrySet()) {
         String typeMappingKey = paramAndArg.getKey();
         String typeParam = typeMappingKey.substring(typeMappingKey.indexOf(':') + 1) + ';';
         String typeArg = paramAndArg.getValue() + ';';
         resolvedTypeDesc = resolvedTypeDesc.replace(typeParam, typeArg);
      }

      return resolvedTypeDesc;
   }

   @Nonnull
   public Type resolveTypeVariable(@Nonnull TypeVariable<?> typeVariable)
   {
      String typeVarKey = getTypeVariableKey(typeVariable);
      Type typeArgument = typeParametersToTypeArguments.get(typeVarKey);

      if (typeArgument == null) {
         typeArgument = typeVariable.getBounds()[0];
      }

      if (typeArgument instanceof TypeVariable<?>) {
         typeArgument = resolveTypeVariable((TypeVariable<?>) typeArgument);
      }

      return typeArgument;
   }

   @Nonnull
   private String getTypeVariableKey(@Nonnull TypeVariable<?> typeVariable)
   {
      String ownerClassDesc = getOwnerClassDesc(typeVariable);
      return ownerClassDesc + ':' + typeVariable.getName();
   }

   @Nonnull
   private String getOwnerClassDesc(@Nonnull TypeVariable<?> typeVariable)
   {
      GenericDeclaration owner = typeVariable.getGenericDeclaration();
      Class<?> ownerClass = owner instanceof Member ? ((Member) owner).getDeclaringClass() : (Class<?>) owner;
      return getOwnerClassDesc(ownerClass);
   }

   @Nonnull
   public String resolveReturnType(@Nonnull String genericSignature)
   {
      int p = genericSignature.lastIndexOf(')') + 1;

      if (typeParametersToTypeArgumentNames.isEmpty() && genericSignature.charAt(0) != '<') {
         return genericSignature.substring(p);
      }

      int q = genericSignature.length();
      String returnType = genericSignature.substring(p, q);
      String resolvedReturnType = resolveReturnType(ownerType, returnType);
      String resolvedSignature;

      if (resolvedReturnType == null) {
         resolvedSignature = returnType;
      }
      else if (resolvedReturnType.charAt(0) == '[') {
         return resolvedReturnType;
      }
      else {
         StringBuilder finalSignature = new StringBuilder(genericSignature);
         finalSignature.replace(p, q, resolvedReturnType);
         resolvedSignature = finalSignature.toString();
      }

      p = resolvedSignature.indexOf(')');
      return resolvedSignature.substring(p + 1);
   }

   @Nullable
   private String resolveReturnType(@Nonnull Class<?> ownerType, @Nonnull String genericReturnType)
   {
      String ownerTypeDesc = getOwnerClassDesc(ownerType);
      String resolvedReturnType = replaceTypeParametersWithActualTypes(ownerTypeDesc, genericReturnType);

      if (!resolvedReturnType.equals(genericReturnType)) {
         return resolvedReturnType;
      }

      if (ownerType.isInterface()) {
         for (Class<?> superInterface: ownerType.getInterfaces()) {
            resolvedReturnType = resolveReturnType(superInterface, genericReturnType);

            if (resolvedReturnType != null) {
               return resolvedReturnType;
            }
         }

         return null;
      }

      Class<?> superClass = ownerType.getSuperclass();
      return superClass == Object.class ? null : resolveReturnType(superClass, genericReturnType);
   }

   public boolean areMatchingTypes(@Nonnull Type declarationType, @Nonnull Type realizationType)
   {
      if (declarationType.equals(realizationType)) {
         return true;
      }

      if (declarationType instanceof TypeVariable<?>) {
         if (realizationType instanceof TypeVariable<?>) {
            return false;
         }

         if (areMatchingTypes((TypeVariable<?>) declarationType, realizationType)) {
            return true;
         }
      }

      return
         declarationType instanceof ParameterizedType && realizationType instanceof ParameterizedType &&
         areMatchingTypes((ParameterizedType) declarationType, (ParameterizedType) realizationType);
   }

   private boolean areMatchingTypes(@Nonnull TypeVariable<?> declarationType, @Nonnull Type realizationType)
   {
      String typeVarKey = getTypeVariableKey(declarationType);
      Type resolvedType = typeParametersToTypeArguments.get(typeVarKey);
      return resolvedType.equals(realizationType) || typeSatisfiesResolvedTypeVariable(resolvedType, realizationType);
   }

   private boolean areMatchingTypes(
      @Nonnull ParameterizedType declarationType, @Nonnull ParameterizedType realizationType)
   {
      return
         declarationType.getRawType().equals(realizationType.getRawType()) &&
         haveMatchingActualTypeArguments(declarationType, realizationType);
   }

   private boolean haveMatchingActualTypeArguments(
      @Nonnull ParameterizedType declarationType, @Nonnull ParameterizedType realizationType)
   {
      Type[] declaredTypeArguments = declarationType.getActualTypeArguments();
      Type[] concreteTypeArguments = realizationType.getActualTypeArguments();

      for (int i = 0, n = declaredTypeArguments.length; i < n; i++) {
         Type declaredTypeArg = declaredTypeArguments[i];
         Type concreteTypeArg = concreteTypeArguments[i];

         if (declaredTypeArg instanceof TypeVariable<?>) {
            if (areMatchingTypeArguments((TypeVariable<?>) declaredTypeArg, concreteTypeArg)) {
               continue;
            }
         }
         else if (areMatchingTypes(declaredTypeArg, concreteTypeArg)) {
            continue;
         }

         return false;
      }

      return true;
   }

   private boolean areMatchingTypeArguments(@Nonnull TypeVariable<?> declaredType, @Nonnull Type concreteType)
   {
      String typeVarKey = getTypeVariableKey(declaredType);
      Type resolvedType = typeParametersToTypeArguments.get(typeVarKey);

      if (resolvedType != null) {
         if (resolvedType.equals(concreteType)) {
            return true;
         }

         if (
            concreteType instanceof Class<?> &&
            typeSatisfiesResolvedTypeVariable(resolvedType, (Class<?>) concreteType)
         ) {
            return true;
         }

         if (
            concreteType instanceof WildcardType &&
            typeSatisfiesUpperBounds(resolvedType, ((WildcardType) concreteType).getUpperBounds())
         ) {
            return true;
         }
      }
      else if (typeSatisfiesUpperBounds(concreteType, declaredType.getBounds())) {
         return true;
      }

      return false;
   }

   private boolean typeSatisfiesResolvedTypeVariable(@Nonnull Type resolvedType, @Nonnull Type realizationType)
   {
      Class<?> realizationClass = getClassType(realizationType);
      return typeSatisfiesResolvedTypeVariable(resolvedType, realizationClass);
   }

   private boolean typeSatisfiesResolvedTypeVariable(@Nonnull Type resolvedType, @Nonnull Class<?> realizationType)
   {
      Class<?> resolvedClass = getClassType(resolvedType);
      return resolvedClass.isAssignableFrom(realizationType);
   }

   private boolean typeSatisfiesUpperBounds(@Nonnull Type type, @Nonnull Type[] upperBounds)
   {
      Class<?> classType = getClassType(type);

      for (Type upperBound : upperBounds) {
         Class<?> classBound = getClassType(upperBound);

         if (!classBound.isAssignableFrom(classType)) {
            return false;
         }
      }

      return true;
   }
}
