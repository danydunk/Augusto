/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.*;
import java.util.Map.*;
import javax.annotation.*;

import static mockit.internal.util.Utilities.containsReference;

import mockit.internal.expectations.invocation.*;
import mockit.internal.state.*;
import mockit.internal.util.*;

final class PhasedExecutionState
{
   @Nonnull final List<Expectation> strictExpectations;
   @Nonnull final List<Expectation> notStrictExpectations;
   @Nonnull final List<VerifiedExpectation> verifiedExpectations;
   @Nonnull final Map<Object, Object> instanceMap;
   @Nonnull final Map<Object, Object> replacementMap;
   @Nullable private List<?> dynamicMockInstancesToMatch;
   @Nullable private List<Class<?>> mockedTypesToMatchOnInstances;

   PhasedExecutionState()
   {
      strictExpectations = new ArrayList<Expectation>();
      notStrictExpectations = new ArrayList<Expectation>();
      verifiedExpectations = new ArrayList<VerifiedExpectation>();
      instanceMap = new IdentityHashMap<Object, Object>();
      replacementMap = new IdentityHashMap<Object, Object>();
   }

   void setDynamicMockInstancesToMatch(@Nonnull List<?> dynamicMockInstancesToMatch)
   {
      this.dynamicMockInstancesToMatch = dynamicMockInstancesToMatch;
   }

   void discoverMockedTypesToMatchOnInstances(@Nonnull List<Class<?>> targetClasses)
   {
      int numClasses = targetClasses.size();

      if (numClasses > 1) {
         for (int i = 0; i < numClasses; i++) {
            Class<?> targetClass = targetClasses.get(i);

            if (targetClasses.lastIndexOf(targetClass) > i) {
               addMockedTypeToMatchOnInstance(targetClass);
            }
         }
      }
   }

   private void addMockedTypeToMatchOnInstance(@Nonnull Class<?> mockedType)
   {
      if (mockedTypesToMatchOnInstances == null) {
         mockedTypesToMatchOnInstances = new LinkedList<Class<?>>();
      }

      if (!containsReference(mockedTypesToMatchOnInstances, mockedType)) {
         mockedTypesToMatchOnInstances.add(mockedType);
      }
   }

   void addExpectation(@Nonnull Expectation expectation, boolean strict)
   {
      ExpectedInvocation invocation = expectation.invocation;
      forceMatchingOnMockInstanceIfRequired(invocation);
      removeMatchingExpectationsCreatedBefore(invocation);

      if (strict) {
         strictExpectations.add(expectation);
      }
      else {
         notStrictExpectations.add(expectation);
      }
   }

   private void forceMatchingOnMockInstanceIfRequired(@Nonnull ExpectedInvocation invocation)
   {
      if (isToBeMatchedOnInstance(invocation.instance, invocation.getMethodNameAndDescription())) {
         invocation.matchInstance = true;
      }
   }

   boolean isToBeMatchedOnInstance(@Nullable Object mock, @Nonnull String mockNameAndDesc)
   {
      if (mock == null || mockNameAndDesc.charAt(0) == '<') {
         return false;
      }

      if (dynamicMockInstancesToMatch != null && containsReference(dynamicMockInstancesToMatch, mock)) {
         return true;
      }

      if (mockedTypesToMatchOnInstances != null) {
         Class<?> mockedClass = GeneratedClasses.getMockedClass(mock);

         if (containsReference(mockedTypesToMatchOnInstances, mockedClass)) {
            return true;
         }
      }
      else if (TestRun.getExecutingTest().isInjectableMock(mock)) {
         return true;
      }

      return false;
   }

   private void removeMatchingExpectationsCreatedBefore(@Nonnull ExpectedInvocation invocation)
   {
      Expectation previousExpectation = findPreviousNotStrictExpectation(invocation);

      if (previousExpectation != null) {
         notStrictExpectations.remove(previousExpectation);
         invocation.copyDefaultReturnValue(previousExpectation.invocation);
      }
   }

   @Nullable
   private Expectation findPreviousNotStrictExpectation(@Nonnull ExpectedInvocation newInvocation)
   {
      int n = notStrictExpectations.size();

      if (n == 0) {
         return null;
      }

      Object mock = newInvocation.instance;
      String mockClassDesc = newInvocation.getClassDesc();
      String mockNameAndDesc = newInvocation.getMethodNameAndDescription();
      boolean constructorInvocation = newInvocation.isConstructor();

      for (int i = 0; i < n; i++) {
         Expectation previousExpectation = notStrictExpectations.get(i);

         if (
            isMatchingInvocation(mock, mockClassDesc, mockNameAndDesc, constructorInvocation, previousExpectation) &&
            isWithMatchingArguments(newInvocation, previousExpectation.invocation)
         ) {
            return previousExpectation;
         }
      }

      return null;
   }

   private boolean isMatchingInvocation(
      @Nullable Object mock, @Nonnull String mockClassDesc, @Nonnull String mockNameAndDesc,
      boolean constructorInvocation, @Nonnull Expectation expectation)
   {
      ExpectedInvocation invocation = expectation.invocation;
      return
         invocation.isMatch(mock, mockClassDesc, mockNameAndDesc) &&
         isSameMockedClass(mock, invocation.instance) &&
         (constructorInvocation || mock == null || isMatchingInstance(mock, expectation));
   }

   private boolean isSameMockedClass(@Nullable Object mock1, @Nullable Object mock2)
   {
      if (mock1 == mock2) {
         return true;
      }

      if (mock1 != null && mock2 != null) {
         Class<?> mockedClass1 = mock1.getClass();
         Class<?> mockedClass2 = GeneratedClasses.getMockedClass(mock2);

         return
            mockedClass2.isAssignableFrom(mockedClass1) ||
            TestRun.getExecutingTest().isInvokedInstanceEquivalentToCapturedInstance(mock1, mock2) ||
            TestRun.mockFixture().areCapturedClasses(mockedClass1, mockedClass2);
      }

      return false;
   }

   private boolean isWithMatchingArguments(
      @Nonnull ExpectedInvocation newInvocation, @Nonnull ExpectedInvocation previousInvocation)
   {
      InvocationArguments newArguments = newInvocation.arguments;
      InvocationArguments previousArguments = previousInvocation.arguments;

      if (newArguments.getMatchers() == null) {
         return previousArguments.isMatch(newArguments.getValues(), instanceMap);
      }

      return newArguments.hasEquivalentMatchers(previousArguments);
   }

   @Nullable
   Expectation findNotStrictExpectation(
      @Nullable Object mock, @Nonnull String mockClassDesc, @Nonnull String mockNameAndDesc, @Nonnull Object[] args)
   {
      boolean constructorInvocation = mockNameAndDesc.charAt(0) == '<';
      Expectation replayExpectationFound = null;

      // Note: new expectations might get added to the list, so a regular loop would cause a CME:
      for (int i = 0, n = notStrictExpectations.size(); i < n; i++) {
         Expectation expectation = notStrictExpectations.get(i);

         if (replayExpectationFound != null && expectation.recordPhase == null) {
            continue;
         }

         if (
            isMatchingInvocation(mock, mockClassDesc, mockNameAndDesc, constructorInvocation, expectation) &&
            expectation.invocation.arguments.isMatch(args, instanceMap)
         ) {
            if (expectation.recordPhase == null) {
               replayExpectationFound = expectation;
               continue;
            }

            if (constructorInvocation) {
               registerReplacementInstanceIfApplicable(mock, expectation.invocation);
            }

            return expectation;
         }
      }

      return replayExpectationFound;
   }

   private void registerReplacementInstanceIfApplicable(@Nullable Object mock, @Nonnull ExpectedInvocation invocation)
   {
      Object replacementInstance = invocation.replacementInstance;

      if (replacementInstance != null && replacementInstance != invocation.instance) {
         replacementMap.put(mock, replacementInstance);
      }
   }

   private boolean isMatchingInstance(@Nonnull Object invokedInstance, @Nonnull Expectation expectation)
   {
      ExpectedInvocation invocation = expectation.invocation;
      assert invocation.instance != null;

      if (isEquivalentInstance(invocation.instance, invokedInstance)) {
         return true;
      }

      if (TestRun.getExecutingTest().isInjectableMock(invokedInstance)) {
         return false;
      }

      if (dynamicMockInstancesToMatch != null) {
         if (containsReference(dynamicMockInstancesToMatch, invokedInstance)) {
            return false;
         }

         Class<?> invokedClass = invocation.instance.getClass();

         for (Object dynamicMock : dynamicMockInstancesToMatch) {
            if (dynamicMock.getClass() == invokedClass) {
               return false;
            }
         }
      }

      return !invocation.matchInstance && expectation.recordPhase != null;
   }

   boolean isEquivalentInstance(@Nonnull Object invocationInstance, @Nonnull Object invokedInstance)
   {
      return
         invocationInstance == invokedInstance ||
         invocationInstance == replacementMap.get(invokedInstance) ||
         invocationInstance == instanceMap.get(invokedInstance) ||
         invokedInstance == instanceMap.get(invocationInstance) ||
         TestRun.getExecutingTest().isInvokedInstanceEquivalentToCapturedInstance(invocationInstance, invokedInstance);
   }

   boolean areInDifferentEquivalenceSets(@Nonnull Object mock1, @Nonnull Object mock2)
   {
      if (mock1 == mock2 || instanceMap.isEmpty()) {
         return false;
      }

      Object mock1Equivalent = instanceMap.get(mock1);
      Object mock2Equivalent = instanceMap.get(mock2);

      if (mock1Equivalent == mock2 || mock2Equivalent == mock1) {
         return false;
      }

      //noinspection SimplifiableIfStatement
      if (mock1Equivalent != null && mock2Equivalent != null) {
         return true;
      }

      return instanceMapHasMocksInSeparateEntries(mock1, mock2);
   }

   private boolean instanceMapHasMocksInSeparateEntries(@Nonnull Object mock1, @Nonnull Object mock2)
   {
      boolean found1 = false;
      boolean found2 = false;

      for (Entry<Object, Object> entry : instanceMap.entrySet()) {
         if (!found1 && isInMapEntry(entry, mock1)) {
            found1 = true;
         }

         if (!found2 && isInMapEntry(entry, mock2)) {
            found2 = true;
         }

         if (found1 && found2) {
            return true;
         }
      }

      return false;
   }

   private static boolean isInMapEntry(@Nonnull Entry<Object, Object> mapEntry, @Nonnull Object mock)
   {
      return mapEntry.getKey() == mock || mapEntry.getValue() == mock;
   }

   @Nullable
   Object getReplacementInstanceForMethodInvocation(@Nonnull Object invokedInstance, @Nonnull String methodNameAndDesc)
   {
      return methodNameAndDesc.charAt(0) == '<' ? null : replacementMap.get(invokedInstance);
   }

   void validateReplacementInstances()
   {
      Map<Class<?>, Object> mockedClassesToReplacements = findMockedClassesHavingReplacementInstances();

      if (mockedClassesToReplacements != null) {
         validateNoMockedClassWithSingleReplacementInstance(mockedClassesToReplacements);
      }
   }

   @Nullable
   private Map<Class<?>, Object> findMockedClassesHavingReplacementInstances()
   {
      ExecutingTest executingTest = TestRun.getExecutingTest();
      Map<Class<?>, Object> mockedClassesToReplacements = null;

      for (Expectation expectation : notStrictExpectations) {
         ExpectedInvocation invocation = expectation.invocation;
         Object replacementInstance = invocation.replacementInstance;

         if (
            replacementInstance != null && replacementInstance != invocation.instance &&
            executingTest.isRegularMockedInstance(replacementInstance)
         ) {
            Class<?> mockedClass = replacementInstance.getClass();

            if (mockedClassesToReplacements == null) {
               mockedClassesToReplacements = new IdentityHashMap<Class<?>, Object>();
               mockedClassesToReplacements.put(mockedClass, replacementInstance);
            }
            else if (!mockedClassesToReplacements.containsKey(mockedClass)) {
               mockedClassesToReplacements.put(mockedClass, replacementInstance);
            }
            else if (mockedClassesToReplacements.get(mockedClass) != replacementInstance) {
               mockedClassesToReplacements.put(mockedClass, null);
            }
         }
      }

      return mockedClassesToReplacements;
   }

   private static void validateNoMockedClassWithSingleReplacementInstance(
      @Nonnull Map<Class<?>, Object> mockedClassesToReplacements)
   {
      for (Entry<Class<?>, Object> mockedClassAndReplacement : mockedClassesToReplacements.entrySet()) {
         if (mockedClassAndReplacement.getValue() != null) {
            Class<?> mockedClass = mockedClassAndReplacement.getKey();
            throw new IllegalStateException("Invalid single replacement of a mocked instance for " + mockedClass);
         }
      }
   }
}
