/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package java8testing;

import java.util.*;
import java.util.function.*;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;
import mockit.internal.startup.*;

public final class DynamicMockingTest
{
   @Test
   public void dynamicallyMockLambdaObject()
   {
      Supplier<String> s = () -> "test";
      new Expectations(s) {};

      assertEquals("test", s.get());
      new Verifications() {{ s.get(); }};
   }

   static class SomeClass
   {
      static String doSomethingStatic() { return "test1"; }
      String doSomething() { return "test2"; }
   }

   @Test
   public void dynamicallyMockReferenceToStaticMethod()
   {
      Supplier<String> s = SomeClass::doSomethingStatic;

      new Expectations(s) {};

      assertEquals("test1", s.get());
      new VerificationsInOrder() {{ s.get(); }};
   }

   @Ignore(
      "Fails on loadClass('...$$Lambda$1...' with ClassNotFoundException due to lambda not getting registered" +
      "with the system class loader; a JVM bug, apparently.")
   @Test
   public void dynamicallyMockReferenceToInstanceMethod() throws Exception
   {
      Supplier<String> s = new SomeClass()::doSomething;

      new Expectations(s) {};

      assertEquals("test2", s.get());
//      new VerificationsInOrder() {{ s.get(); }};
   }

   @Ignore("Similar to previous test - apparent JVM issue")
   @Test
   public void dynamicallyMockLambdaObjectWithCapturedVariable()
   {
      SomeClass toBeCaptured = new SomeClass();
      //noinspection Convert2MethodRef
      Supplier<String> s = () -> toBeCaptured.doSomething();

      Startup.retransformClass(s.getClass());
//      new Expectations(s) {};

      assertEquals("test2", s.get());
//      new Verifications() {{ s.get(); }};
   }

   final List<String> aList = new ArrayList<>();

   @Ignore(
      "Fails with VerifyError because the JVM tries to load a non-existent class of name $$Lambda$1;" +
      "apparently, the JVM wants to load an 'owner' object which doesn't exist.")
   @Test
   public void dynamicallyMockLambdaObjectWithCapturedField()
   {
      LongSupplier s = () -> aList.stream().count();

//      Startup.retransformClass(s.getClass());
      new Expectations(s) {{ s.getAsLong(); result = 2; }};

      assertEquals(2, s.getAsLong());
   }
}
