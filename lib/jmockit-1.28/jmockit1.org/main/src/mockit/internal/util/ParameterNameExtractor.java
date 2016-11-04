/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

import javax.annotation.*;

import mockit.external.asm.*;
import mockit.internal.*;
import mockit.internal.state.*;
import static mockit.external.asm.ClassReader.*;
import static mockit.external.asm.Opcodes.*;

public final class ParameterNameExtractor extends ClassVisitor
{
   @Nonnull private String classDesc;
   @Nonnegative private int memberAccess;
   @Nonnull private String memberName;
   @Nonnull private String memberDesc;

   public ParameterNameExtractor()
   {
      classDesc = memberName = memberDesc = "";
   }

   @Nonnull
   public String extractNames(@Nonnull Class<?> classOfInterest)
   {
      String className = classOfInterest.getName();
      classDesc = className.replace('.', '/');

      if (!ParameterNames.hasNamesForClass(classDesc)) {
         // Reads class from file, since JRE 1.6 (but not 1.7) discards parameter names on retransformation.
         ClassReader cr = ClassFile.readFromFile(classDesc);
         cr.accept(this, SKIP_FRAMES);
      }

      return classDesc;
   }

   @Nullable @Override
   public MethodVisitor visitMethod(
      int access, @Nonnull String name, @Nonnull String desc, @Nullable String signature, @Nullable String[] exceptions)
   {
      if ((access & ACC_SYNTHETIC) == 0) {
         memberAccess = access;
         memberName = name;
         memberDesc = desc;
         return new MethodOrConstructorVisitor();
      }

      return null;
   }

   private final class MethodOrConstructorVisitor extends MethodVisitor
   {
      @Override
      public void visitLocalVariable(
         @Nonnull String name, @Nonnull String desc, String signature, Label start, Label end, @Nonnegative int index)
      {
         ParameterNames.registerName(classDesc, memberAccess, memberName, memberDesc, desc, name, index);
      }
   }
}
