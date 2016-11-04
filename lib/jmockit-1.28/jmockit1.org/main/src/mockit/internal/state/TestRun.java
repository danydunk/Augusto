/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import javax.annotation.*;

import mockit.internal.expectations.*;
import mockit.internal.injection.*;
import mockit.internal.expectations.mocking.*;
import mockit.internal.mockups.*;
import mockit.internal.util.*;

/**
 * A singleton which stores several data structures which in turn hold global state for individual test methods, test
 * classes, and for the test run as a whole.
 */
public final class TestRun
{
   private static final TestRun INSTANCE = new TestRun();
   private TestRun() {}

   // Fields with global state ////////////////////////////////////////////////////////////////////////////////////////

   private static final ThreadLocal<Integer> noMockingCount = new ThreadLocal<Integer>() {
      @Override protected Integer initialValue() { return 0; }
      @Override public void set(Integer valueToAdd) { super.set(get() + valueToAdd); }
   };

   // Used only by the Coverage tool:
   private int testId;

   @Nullable private Class<?> currentTestClass;
   @Nullable private Object currentTestInstance;
   private boolean settingUpNextTest;
   @Nullable private FieldTypeRedefinitions fieldTypeRedefinitions;
   @Nullable private TestedClassInstantiations testedClassInstantiations;

   @Nonnull private final MockFixture mockFixture = new MockFixture();

   @Nonnull private final ExecutingTest executingTest = new ExecutingTest();
   @Nonnull private final MockClasses mockClasses = new MockClasses();

   // Static "getters" for global state ///////////////////////////////////////////////////////////////////////////////

   public static boolean isInsideNoMockingZone() { return noMockingCount.get() > 0; }

   @Nullable public static Class<?> getCurrentTestClass() { return INSTANCE.currentTestClass; }

   @Nullable public static Object getCurrentTestInstance() { return INSTANCE.currentTestInstance; }
   public static boolean isSettingUpNextTest() { return INSTANCE.settingUpNextTest; }

   public static int getTestId() { return INSTANCE.testId; }

   @Nullable
   public static FieldTypeRedefinitions getFieldTypeRedefinitions() { return INSTANCE.fieldTypeRedefinitions; }

   @Nullable
   public static TestedClassInstantiations getTestedClassInstantiations() { return INSTANCE.testedClassInstantiations; }

   @Nonnull public static MockFixture mockFixture() { return INSTANCE.mockFixture; }

   @Nonnull public static ExecutingTest getExecutingTest() { return INSTANCE.executingTest; }

   @Nullable public static RecordAndReplayExecution getRecordAndReplayForRunningTest()
   {
      return INSTANCE.executingTest.getCurrentRecordAndReplay();
   }

   @Nonnull
   public static RecordAndReplayExecution getOrCreateRecordAndReplayForRunningTest()
   {
      return INSTANCE.executingTest.getOrCreateRecordAndReplay();
   }

   @Nonnull
   public static RecordAndReplayExecution getRecordAndReplayForVerifications()
   {
      if (INSTANCE.fieldTypeRedefinitions == null) {
         IllegalStateException failure = new IllegalStateException("Invalid place to verify expectations");
         StackTrace.filterStackTrace(failure);
         throw failure;
      }

      return INSTANCE.executingTest.getRecordAndReplayForVerifications();
   }

   @Nonnull public static MockClasses getMockClasses() { return INSTANCE.mockClasses; }
   @Nonnull public static MockStates getMockStates() { return INSTANCE.mockClasses.mockStates; }

   // Static "mutators" for global state //////////////////////////////////////////////////////////////////////////////

   public static void setCurrentTestClass(@Nullable Class<?> testClass)
   {
      INSTANCE.currentTestClass = testClass;
   }

   public static void prepareForNextTest()
   {
      INSTANCE.testId++;
      INSTANCE.executingTest.setRecordAndReplay(null);
   }

   public static void enterNoMockingZone() { noMockingCount.set(1); }
   public static void exitNoMockingZone()  { noMockingCount.set(-1); }
   public static void clearNoMockingZone() { noMockingCount.remove(); }

   public static void clearCurrentTestInstance()
   {
      INSTANCE.currentTestInstance = null;
      INSTANCE.settingUpNextTest = false;
   }

   public static void setRunningIndividualTest(@Nonnull Object testInstance, boolean settingUpNextTest)
   {
      INSTANCE.currentTestInstance = testInstance;
      INSTANCE.settingUpNextTest = settingUpNextTest;
   }

   public static void setFieldTypeRedefinitions(@Nullable FieldTypeRedefinitions redefinitions)
   {
      INSTANCE.fieldTypeRedefinitions = redefinitions;
   }

   public static void setTestedClassInstantiations(@Nullable TestedClassInstantiations testedClassInstantiations)
   {
      INSTANCE.testedClassInstantiations = testedClassInstantiations;
   }

   public static void finishCurrentTestExecution()
   {
      INSTANCE.executingTest.finishExecution();
   }

   // Methods to be called only from generated bytecode or from the MockingBridge /////////////////////////////////////

   @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "SimplifiableIfStatement"})
   public static boolean updateMockState(
      @Nonnull String mockUpClassDesc, @Nullable Object mockedInstance, int mockStateIndex)
   {
      Object mockUp = getMock(mockUpClassDesc, mockedInstance);

      if (mockUp == null) {
         return false;
      }

      if (mockStateIndex < 0) {
         return true;
      }

      return getMockStates().updateMockState(mockUp, mockStateIndex);
   }

   @Nullable
   public static Object getMock(@Nonnull String mockUpClassDesc, @Nullable Object mockedInstance)
   {
      return INSTANCE.mockClasses.getMock(mockUpClassDesc, mockedInstance);
   }
}
