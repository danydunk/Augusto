/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Arrays.asList;

import org.junit.*;
import static org.junit.Assert.*;

public final class GenericMockedTypesTest
{
   @Mocked Callable<Integer> mock2;

   @Test
   public void mockGenericInterfaces(@Mocked final Callable<?> mock1) throws Exception
   {
      Class<?> mockedClass1 = mock1.getClass();
      assertEquals(1, mockedClass1.getGenericInterfaces().length);

      new Expectations() {{
         Class<?> mockedClass2 = mock2.getClass();

         ParameterizedType genericType2 = (ParameterizedType) mockedClass2.getGenericInterfaces()[0];
         assertSame(Callable.class, genericType2.getRawType());
         assertSame(Integer.class, genericType2.getActualTypeArguments()[0]);

         Method mockedMethod = mockedClass2.getDeclaredMethod("call");
         assertSame(Integer.class, mockedMethod.getGenericReturnType());

         mock1.call(); result = "mocked";
         mock2.call(); result = 123;
      }};

      assertEquals("mocked", mock1.call());
      assertEquals(123, mock2.call().intValue());
   }

   @Test
   public void obtainGenericSuperclassFromClassGeneratedForNonGenericInterface(@Mocked Runnable mock)
   {
      Class<?> generatedClass = mock.getClass();
      Type genericSuperClass = generatedClass.getGenericSuperclass();

      // At one time, a "GenericSignatureFormatError: Signature Parse error: expected a class type
      // Remaining input: nullLjava/lang/Runnable;" would occur.
      assertSame(Object.class, genericSuperClass);
   }

   @Test
   public void mockGenericAbstractClass(@Mocked final Dictionary<Integer, String> mock) throws Exception
   {
      new Expectations() {{
         Class<?> mockedClass = mock.getClass();

         ParameterizedType genericBase = (ParameterizedType) mockedClass.getGenericSuperclass();
         assertSame(Dictionary.class, genericBase.getRawType());
         assertSame(Integer.class, genericBase.getActualTypeArguments()[0]);
         assertSame(String.class, genericBase.getActualTypeArguments()[1]);

         Method mockedMethod1 = mockedClass.getDeclaredMethod("keys");
         assertEquals("java.util.Enumeration<java.lang.Integer>", mockedMethod1.getGenericReturnType().toString());

         Method mockedMethod2 = mockedClass.getDeclaredMethod("elements");
         assertEquals("java.util.Enumeration<java.lang.String>", mockedMethod2.getGenericReturnType().toString());

         Method mockedMethod3 = mockedClass.getDeclaredMethod("put", Object.class, Object.class);
         assertSame(String.class, mockedMethod3.getGenericReturnType());

         mock.put(123, "test"); result = "mocked";
      }};

      assertEquals("mocked", mock.put(123, "test"));
   }

   @Test
   public void mockRawMapInterface(@SuppressWarnings("rawtypes") @Mocked final Map rawMap)
   {
      new Expectations() {{
         rawMap.get("test");
         result = new Object();
      }};

      Object value = rawMap.get("test");
      assertNotNull(value);
   }

   public interface InterfaceWithMethodParametersMixingGenericTypesAndArrays
   {
      <T> void doSomething(int[] i, T b);
      void doSomething(Callable<int[]> pc, int[] ii);
      void doSomething(Callable<String> pc, int[] i, boolean[] currencies, int[] ii);
   }

   @Test
   public void mockMethodsHavingGenericsAndArrays(@Mocked InterfaceWithMethodParametersMixingGenericTypesAndArrays mock)
   {
      mock.doSomething((Callable<int[]>) null, new int[] {1, 2});
      mock.doSomething(null, new int[] {1, 2}, null, new int[] {3, 4, 5});
   }

   public interface NonGenericInterfaceWithGenericMethods
   {
      <T> T genericMethodWithUnboundedReturnType();
      <T extends CharSequence> T genericMethodWithBoundedReturnType();
   }

   @Test
   public void resultFromGenericMethodsOfNonGenericInterface(@Mocked final NonGenericInterfaceWithGenericMethods mock)
   {
      new Expectations() {{
         mock.genericMethodWithUnboundedReturnType(); result = 123;
         mock.genericMethodWithBoundedReturnType(); result = "test";
      }};

      Object v1 = mock.genericMethodWithUnboundedReturnType();
      assertEquals(123, v1);

      Object v2 = mock.genericMethodWithBoundedReturnType();
      assertEquals("test", v2);
   }

   static class Item implements Serializable {}
   static class GenericContainer<T extends Serializable> { final T getItem() { return null; } }

   @Test
   public void createFirstLevelCascadedMockFromTypeParameter(@Mocked GenericContainer<Item> mockContainer)
   {
      Serializable mock = mockContainer.getItem();

      assertSame(Item.class, mock.getClass());
   }

   static class Factory1 { static GenericContainer<Item> getContainer() { return null; } }

   @Test
   public void createSecondLevelCascadedMockFromTypeParameterInGenericMethodResolvedFromFirstLevelReturnType(
      @Mocked Factory1 mockFactory)
   {
      GenericContainer<Item> mockContainer = Factory1.getContainer();
      Serializable cascadedMock = mockContainer.getItem();

      assertNotNull(cascadedMock);
      assertSame(Item.class, cascadedMock.getClass());
   }

   static class ConcreteContainer extends GenericContainer<Item> {}
   static class Factory2 { ConcreteContainer getContainer() { return null; } }

   @Test
   public void createSecondLevelCascadedMockFromTypeParameterInBaseTypeOfMethodReturn(@Mocked Factory2 mockFactory)
   {
      ConcreteContainer mockContainer = mockFactory.getContainer();
      Serializable cascadedMock = mockContainer.getItem();

      assertSame(Item.class, cascadedMock.getClass());
   }

   static class Collaborator { Runnable doSomething() { return null; } }
   static class Collaborator2 {}

   @Test
   public void cascadingClassWithNameStartingWithAnotherMockedClass(
      @Mocked final Collaborator regularMock, @Mocked Collaborator2 cascadingMock)
   {
      new Expectations() {{
         regularMock.doSomething();
      }};

      assertNotNull(regularMock.doSomething());
   }

   public interface InterfaceWithBoundedTypeParameter<T extends Runnable> { T getFoo(); }

   @Test
   public void createCascadedMockFromGenericInterfaceMethodWhichReturnsBoundedTypeParameter(
      @Mocked InterfaceWithBoundedTypeParameter<?> mock)
   {
      Runnable foo = mock.getFoo();
      assertNotNull(foo);
      foo.run();
   }

   public interface InterfaceWhichExtendsInterfaceWithBoundedTypeParameter<T extends Runnable>
      extends InterfaceWithBoundedTypeParameter<T> {}

   @Test
   public void createCascadedMockFromGenericMethodDefinedInSuperInterfaceWithBoundedTypeParameter(
      @Mocked InterfaceWhichExtendsInterfaceWithBoundedTypeParameter<?> mock)
   {
      Runnable foo = mock.getFoo();
      assertNotNull(foo);
      foo.run();
   }

   static class Abc {}
   static class GenericBase<T> { T doSomething() { return null; } }
   static class GenericSubclass<T> extends GenericBase<T> { T getAbc() { return null; } }

   @Test
   public void createCascadedMockFromGenericSubclassHavingSameTypeParameterNameAsBaseClass(
      @Mocked GenericSubclass<Abc> mock)
   {
      Abc abc = mock.getAbc();
      assertNotNull(abc);
   }

   @Test
   public void mockGenericClassHavingTypeArgumentOfArrayType(@Mocked GenericBase<String[]> mock)
   {
      String[] result = mock.doSomething();

      assertEquals(0, result.length);
   }

   @Test
   public void mockGenericClassHavingTypeArgumentOfArrayTypeWithPrimitiveComponent(@Mocked GenericBase<int[]> mock)
   {
      int[] result = mock.doSomething();

      assertEquals(0, result.length);
   }

   @Test
   public void mockGenericClassHavingTypeArgumentOfArrayTypeWith2DPrimitiveComponent(@Mocked GenericBase<int[][]> mock)
   {
      int[][] result = mock.doSomething();

      assertEquals(0, result.length);
   }

   @Test
   public void mockGenericClassHavingTypeArgumentOfArrayTypeWithGenericComponent(@Mocked GenericBase<List<?>[]> mock)
   {
      List<?>[] result = mock.doSomething();

      assertEquals(0, result.length);
   }

   static final class DerivedClass extends GenericBase<Number[]> {}

   @Test
   public void mockClassExtendingAGenericBaseClassHavingTypeArgumentOfArrayType(@Mocked DerivedClass mock)
   {
      Number[] result = mock.doSomething();

      assertEquals(0, result.length);
   }

   public interface BaseGenericInterface<V> { V doSomething(); }
   public interface DerivedGenericInterface<V> extends BaseGenericInterface<List<V>> { V doSomethingElse(); }

   @Test
   public void recordGenericInterfaceMethodWithReturnTypeGivenByTypeParameterDependentOnAnotherTypeParameterOfSameName(
      @Mocked final DerivedGenericInterface<String> dep) throws Exception
   {
      Class<?> generatedClass = dep.getClass();
      Method mockedBaseMethod = generatedClass.getDeclaredMethod("doSomething");
      Type rt = mockedBaseMethod.getGenericReturnType();
      assertSame(List.class, rt);

      Method mockedSubInterfaceMethod = generatedClass.getDeclaredMethod("doSomethingElse");
      rt = mockedSubInterfaceMethod.getGenericReturnType();
      assertSame(String.class, rt);

      final List<String> values = asList("a", "b");

      new Expectations() {{
         dep.doSomething(); result = values;
         dep.doSomethingElse(); result = "Abc";
      }};

      List<String> resultFromBase = dep.doSomething();
      String resultFromSub = dep.doSomethingElse();

      assertSame(values, resultFromBase);
      assertEquals("Abc", resultFromSub);
   }
}
