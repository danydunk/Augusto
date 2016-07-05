/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import java.lang.instrument.*;
import java.security.*;
import java.util.*;
import javax.annotation.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.state.*;
import mockit.internal.util.*;
import static mockit.external.asm.ClassReader.*;
import static mockit.internal.capturing.CapturedType.*;

public final class CaptureTransformer<M> implements ClassFileTransformer
{
   @Nonnull private final CapturedType capturedType;
   @Nonnull private final String capturedTypeDesc;
   @Nonnull private final CaptureOfImplementations<M> captureOfImplementations;
   @Nonnull private final Map<ClassIdentification, byte[]> transformedClasses;
   @Nullable private final M typeMetadata;
   private boolean inactive;

   CaptureTransformer(
      @Nonnull CapturedType capturedType, @Nonnull CaptureOfImplementations<M> captureOfImplementations,
      boolean registerTransformedClasses, @Nullable M typeMetadata)
   {
      this.capturedType = capturedType;
      capturedTypeDesc = Type.getInternalName(capturedType.baseType);
      this.captureOfImplementations = captureOfImplementations;
      transformedClasses = registerTransformedClasses ?
         new HashMap<ClassIdentification, byte[]>(2) : Collections.<ClassIdentification, byte[]>emptyMap();
      this.typeMetadata = typeMetadata;
   }

   public void deactivate()
   {
      inactive = true;

      if (!transformedClasses.isEmpty()) {
         RedefinitionEngine redefinitionEngine = new RedefinitionEngine();

         for (Map.Entry<ClassIdentification, byte[]> classNameAndOriginalBytecode : transformedClasses.entrySet()) {
            ClassIdentification classId = classNameAndOriginalBytecode.getKey();
            byte[] originalBytecode = classNameAndOriginalBytecode.getValue();
            redefinitionEngine.restoreToDefinition(classId.getLoadedClass(), originalBytecode);
         }

         transformedClasses.clear();
      }
   }

   @Nullable @Override
   public byte[] transform(
      @Nullable ClassLoader loader, @Nonnull String classDesc, @Nullable Class<?> classBeingRedefined,
      @Nullable ProtectionDomain protectionDomain, @Nonnull byte[] classfileBuffer)
   {
      if (classBeingRedefined != null || inactive || isNotToBeCaptured(loader, protectionDomain, classDesc)) {
         return null;
      }

      ClassReader cr = new ClassReader(classfileBuffer);
      SuperTypeCollector superTypeCollector = new SuperTypeCollector(loader);

      try {
         cr.accept(superTypeCollector, SKIP_DEBUG);
      }
      catch (VisitInterruptedException ignore) {
         if (superTypeCollector.classExtendsCapturedType) {
            String className = classDesc.replace('/', '.');
            return modifyAndRegisterClass(loader, className, cr);
         }
      }

      return null;
   }

   @Nonnull
   private byte[] modifyAndRegisterClass(
      @Nullable ClassLoader loader, @Nonnull String className, @Nonnull ClassReader cr)
   {
      ClassVisitor modifier = captureOfImplementations.createModifier(loader, cr, capturedType.baseType, typeMetadata);
      cr.accept(modifier, SKIP_FRAMES);

      ClassIdentification classId = new ClassIdentification(loader, className);
      byte[] originalBytecode = cr.b;

      if (transformedClasses == Collections.<ClassIdentification, byte[]>emptyMap()) {
         TestRun.mockFixture().addTransformedClass(classId, originalBytecode);
      }
      else {
         transformedClasses.put(classId, originalBytecode);
      }

      TestRun.mockFixture().registerMockedClass(capturedType.baseType);
      return modifier.toByteArray();
   }

   private final class SuperTypeCollector extends ClassVisitor
   {
      @Nullable private final ClassLoader loader;
      boolean classExtendsCapturedType;

      private SuperTypeCollector(@Nullable ClassLoader loader) { this.loader = loader; }

      @Override
      public void visit(
         int version, int access, @Nonnull String name, @Nullable String signature, @Nullable String superName,
         @Nullable String[] interfaces)
      {
         classExtendsCapturedType = false;

         if (capturedTypeDesc.equals(superName)) {
            classExtendsCapturedType = true;
            throw VisitInterruptedException.INSTANCE;
         }

         boolean haveInterfaces = interfaces != null && interfaces.length > 0;

         if (haveInterfaces) {
            for (String implementedInterface : interfaces) {
               if (capturedTypeDesc.equals(implementedInterface)) {
                  classExtendsCapturedType = true;
                  throw VisitInterruptedException.INSTANCE;
               }
            }
         }

         if (superName != null) {
            if (!"java/lang/Object mockit/MockUp".contains(superName)) {
               searchSuperType(superName);
            }

            if (haveInterfaces) {
               for (String implementedInterface : interfaces) {
                  searchSuperType(implementedInterface);
               }
            }
         }

         throw VisitInterruptedException.INSTANCE;
      }

      private void searchSuperType(@Nonnull String superName)
      {
         ClassReader cr = ClassFile.createClassFileReader(loader, superName);

         try {
            cr.accept(this, SKIP_DEBUG);
         }
         catch (VisitInterruptedException e) {
            if (classExtendsCapturedType) throw e;
         }
      }
   }

   @Nullable
   public <C extends CaptureOfImplementations<?>> C getCaptureOfImplementationsIfApplicable(@Nonnull Class<?> baseType)
   {
      if (baseType == capturedType.baseType && typeMetadata != null) {
         //noinspection unchecked
         return (C) captureOfImplementations;
      }

      return null;
   }

   public boolean areCapturedClasses(@Nonnull Class<?> mockedClass1, @Nonnull Class<?> mockedClass2)
   {
      Class<?> baseType = capturedType.baseType;
      return baseType.isAssignableFrom(mockedClass1) && baseType.isAssignableFrom(mockedClass2);
   }
}
