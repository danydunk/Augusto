/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.lang.annotation.*;

import org.junit.*;
import org.junit.runners.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class TestedClassWithFullDITest
{
   public static class TestedClass
   {
      Runnable dependencyToBeMocked;
      FirstLevelDependency dependency2;
      FirstLevelDependency dependency3;
      CommonDependency commonDependency;
      String name;
      StringBuilder description;
      final Integer number = null;
      boolean flag = true;
      Thread.State threadState;
      AnotherTestedClass subObj;
      YetAnotherTestedClass subObj2;
   }

   public static class FirstLevelDependency
   {
      String firstLevelId;
      SecondLevelDependency dependency;
      CommonDependency commonDependency;
      Runnable dependencyToBeMocked;
   }

   public static class SecondLevelDependency { CommonDependency commonDependency; }
   public static class CommonDependency {}

   @Tested(fullyInitialized = true)
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   public @interface IntegrationTested {}

   public static class YetAnotherTestedClass {}
   @IntegrationTested YetAnotherTestedClass tested3;

   @IntegrationTested TestedClass tested;
   @Injectable Runnable mockedDependency;

   @Test
   public void useFullyInitializedTestedObjectWithNoInjectableForFirstLevelDependency()
   {
      assertNull(tested.name);
      assertSame(tested.commonDependency, tested.dependency2.dependency.commonDependency);
   }

   @Test
   public void useFullyInitializedTestedObjectWithValueForFirstLevelDependency(@Injectable("test") String id)
   {
      assertEquals("test", tested.name);
      assertNull(tested.description);
      assertNull(tested.number);
      assertTrue(tested.flag);
      assertNull(tested.threadState);
      assertSame(mockedDependency, tested.dependencyToBeMocked);
      assertNotNull(tested.dependency2);
      assertEquals("test", tested.dependency2.firstLevelId);
      assertSame(tested.dependency2, tested.dependency3);
      assertNotNull(tested.commonDependency);
      assertNotNull(tested.dependency2.dependency);
      assertSame(tested.dependency2.dependency, tested.dependency3.dependency);
      assertSame(tested.commonDependency, tested.dependency2.commonDependency);
      assertSame(tested.commonDependency, tested.dependency3.commonDependency);
      assertSame(tested.commonDependency, tested.dependency2.dependency.commonDependency);
      assertSame(mockedDependency, tested.dependency2.dependencyToBeMocked);
      assertSame(mockedDependency, tested.dependency3.dependencyToBeMocked);
   }

   public static class AnotherTestedClass { YetAnotherTestedClass subObj; }
   @IntegrationTested AnotherTestedClass tested2;

   @Test
   public void verifyOtherTestedObjectsGetInjectedIntoFirstOne()
   {
      assertSame(tested2, tested.subObj);
      assertSame(tested3, tested.subObj2);
      assertSame(tested3, tested.subObj.subObj);
   }

   @Tested DependencyImpl concreteDependency;
   @IntegrationTested ClassWithDependencyOfAbstractType tested4;

   public interface Dependency {}
   static class DependencyImpl implements Dependency {}
   static class ClassWithDependencyOfAbstractType { Dependency dependency; }

   @Test
   public void useTestedObjectOfSubtypeForAbstractDependencyTypeInAnotherTestedObject()
   {
      assertSame(concreteDependency, tested4.dependency);
   }
}
