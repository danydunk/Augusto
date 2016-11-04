/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.annotation.*;
import java.lang.reflect.*;
import javax.annotation.*;
import static java.lang.reflect.Modifier.*;

import mockit.*;
import mockit.internal.injection.*;
import mockit.internal.state.*;
import mockit.internal.util.*;
import static mockit.internal.util.AutoBoxing.isWrapperOfPrimitiveType;

@SuppressWarnings("EqualsAndHashcode")
public final class MockedType implements InjectionPointProvider
{
   @Mocked private static final Object DUMMY = null;
   private static final int DUMMY_HASHCODE;

   static
   {
      int h = 0;

      try {
         Field dummy = MockedType.class.getDeclaredField("DUMMY");
         Mocked mocked = dummy.getAnnotation(Mocked.class);
         h = mocked.hashCode();
      }
      catch (NoSuchFieldException ignore) {}

      DUMMY_HASHCODE = h;
   }

   @Nullable public final Field field;
   public final boolean fieldFromTestClass;
   private final int accessModifiers;
   @Nullable private final Mocked mocked;
   @Nullable private final Capturing capturing;
   public final boolean injectable;
   @Nonnull public final Type declaredType;
   @Nonnull public final String mockId;
   @Nullable Object providedValue;

   public MockedType(@Nonnull Field field)
   {
      this.field = field;
      fieldFromTestClass = true;
      accessModifiers = field.getModifiers();
      mocked = field.getAnnotation(Mocked.class);
      capturing = field.getAnnotation(Capturing.class);
      Injectable injectableAnnotation = field.getAnnotation(Injectable.class);
      injectable = injectableAnnotation != null;
      declaredType = field.getGenericType();
      mockId = field.getName();

      validateAnnotationUsage();

      providedValue = getDefaultInjectableValue(injectableAnnotation);
      registerCascadingAsNeeded();
   }

   @Nullable
   private Object getDefaultInjectableValue(@Nullable Injectable annotation)
   {
      if (annotation != null) {
         String value = annotation.value();

         if (!value.isEmpty()) {
            Class<?> injectableClass = getClassType();

            if (injectableClass == TypeVariable.class) {
               // Not supported, do nothing.
            }
            else {
               return Utilities.convertFromString(injectableClass, value);
            }
         }
      }

      return null;
   }

   private void registerCascadingAsNeeded()
   {
      if (isMockableType() && !(declaredType instanceof TypeVariable<?>)) {
         ExecutingTest executingTest = TestRun.getExecutingTest();
         CascadingTypes types = executingTest.getCascadingTypes();
         types.add(fieldFromTestClass, declaredType, null);
      }
   }

   MockedType(
      @Nonnull String testClassDesc, @Nonnull String testMethodDesc, int paramIndex, @Nonnull Type parameterType,
      @Nonnull Annotation[] annotationsOnParameter)
   {
      field = null;
      fieldFromTestClass = false;
      accessModifiers = 0;
      mocked = getAnnotation(annotationsOnParameter, Mocked.class);
      capturing = getAnnotation(annotationsOnParameter, Capturing.class);
      Injectable injectableAnnotation = getAnnotation(annotationsOnParameter, Injectable.class);
      injectable = injectableAnnotation != null;
      declaredType = parameterType;

      String parameterName = ParameterNames.getName(testClassDesc, testMethodDesc, paramIndex);
      mockId = parameterName == null ? "param" + paramIndex : parameterName;

      validateAnnotationUsage();

      providedValue = getDefaultInjectableValue(injectableAnnotation);

      if (providedValue == null && parameterType instanceof Class<?>) {
         Class<?> parameterClass = (Class<?>) parameterType;

         if (parameterClass.isPrimitive()) {
            providedValue = DefaultValues.defaultValueForPrimitiveType(parameterClass);
         }
      }

      registerCascadingAsNeeded();
   }

   @Nullable
   private static <A extends Annotation> A getAnnotation(
      @Nonnull Annotation[] annotations, @Nonnull Class<A> annotation)
   {
      for (Annotation paramAnnotation : annotations) {
         if (paramAnnotation.annotationType() == annotation) {
            //noinspection unchecked
            return (A) paramAnnotation;
         }
      }

      return null;
   }

   private void validateAnnotationUsage()
   {
      if (capturing != null) {
         if (capturing.maxInstances() == Integer.MAX_VALUE) {
            Class<?> baseType = getClassType();
            int modifiers = baseType.getModifiers();

            if (isFinal(modifiers)) {
               throw new IllegalArgumentException("Invalid @Capturing of final " + baseType + ": " + mockId);
            }

            if (injectable) {
               throw new IllegalArgumentException("Invalid application of @Capturing and @Injectable: " + mockId);
            }
         }

         validateAgainstAnnotationRedundancy("@Capturing");
      }
      else if (injectable) {
         validateAgainstAnnotationRedundancy("@Injectable");
      }
   }

   private void validateAgainstAnnotationRedundancy(@Nonnull String otherAnnotation)
   {
      if (mocked != null && !mocked.stubOutClassInitialization()) {
         throw new IllegalArgumentException("Redundant application of @Mocked and " + otherAnnotation + ": " + mockId);
      }
   }

   MockedType(@Nonnull String cascadingMethodName, @Nonnull Type cascadedType)
   {
      field = null;
      fieldFromTestClass = false;
      accessModifiers = 0;
      mocked = null;
      capturing = null;
      injectable = true;
      declaredType = cascadedType;
      mockId = cascadingMethodName;
   }

   @Nonnull @Override public Type getDeclaredType() { return declaredType; }
   @Nonnull @Override public Class<?> getClassOfDeclaredType() { return getClassType(); }
   @Nonnull @Override public String getName() { return mockId; }

   @Nonnull @Override
   public Annotation[] getAnnotations()
   {
      throw new UnsupportedOperationException("Annotations on injectable: not supported yet");
   }

   /**
    * @return the class object corresponding to the type to be mocked, or {@code TypeVariable.class} in case the
    * mocked type is a type variable (which usually occurs when the mocked implements/extends multiple types)
    */
   @Nonnull
   public Class<?> getClassType()
   {
      if (declaredType instanceof Class<?>) {
         return (Class<?>) declaredType;
      }

      if (declaredType instanceof ParameterizedType) {
         ParameterizedType parameterizedType = (ParameterizedType) declaredType;
         return (Class<?>) parameterizedType.getRawType();
      }

      // Occurs when declared type is a TypeVariable, usually having two or more bound types.
      // In such cases, there isn't a single class type.
      return TypeVariable.class;
   }

   boolean isMockableType()
   {
      if (mocked == null && !injectable && capturing == null) {
         return false;
      }

      //noinspection SimplifiableIfStatement
      if (!(declaredType instanceof Class<?>)) {
         return true;
      }

      return isMockableType((Class<?>) declaredType);
   }

   private boolean isMockableType(@Nonnull Class<?> classType)
   {
      if (classType.isPrimitive() || classType.isArray() || classType == Integer.class) {
         return false;
      }

      if (injectable) {
         if (classType == String.class || isWrapperOfPrimitiveType(classType) || classType.isEnum()) {
            return false;
         }
      }

      return true;
   }

   boolean isFinalFieldOrParameter() { return field == null || isFinal(accessModifiers); }
   boolean isClassInitializationToBeStubbedOut() { return mocked != null && mocked.stubOutClassInitialization(); }

   boolean withInstancesToCapture() { return getMaxInstancesToCapture() > 0; }
   int getMaxInstancesToCapture() { return capturing == null ? 0 : capturing.maxInstances(); }

   @Nullable
   public Object getValueToInject(@Nullable Object objectWithFields)
   {
      if (field == null) {
         return providedValue;
      }

      Object value = FieldReflection.getFieldValue(field, objectWithFields);

      if (!injectable) {
         return value;
      }

      if (value == null) {
         return providedValue;
      }

      if (providedValue == null) {
         return value;
      }

      Class<?> fieldType = field.getType();

      if (!fieldType.isPrimitive()) {
         return value;
      }

      Object defaultValue = DefaultValues.defaultValueForPrimitiveType(fieldType);

      return value.equals(defaultValue) ? providedValue : value;
   }

   @Nullable @Override
   public Object getValue(@Nullable Object owner) { return getValueToInject(owner); }

   @Override
   public int hashCode()
   {
      int result = declaredType.hashCode();

      if (isFinal(accessModifiers)) {
         result *= 31;
      }

      if (injectable) {
         result *= 37;
      }

      if (mocked != null) {
         int h = mocked.hashCode();

         if (h != DUMMY_HASHCODE) {
            result = 31 * result + h;
         }
      }

      return result;
   }
}
