/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.util.*;
import javax.annotation.*;

import mockit.internal.expectations.*;

/**
 * Used to <em>record</em> expectations on {@linkplain Mocked mocked} types and their instances.
 * <p/>
 * Each recorded expectation is intended to match one or more method or constructor invocations, that we expect will
 * occur during the execution of code under test.
 * When a match is detected, the recorded {@linkplain #result result} is returned to the caller.
 * Alternatively, a recorded exception/error is thrown, or an arbitrary {@linkplain Delegate delegate} method is
 * executed.
 * <p/>
 * Expectations are recorded simply by invoking the desired method or constructor on the mocked type/instance, during
 * the initialization of an {@code Expectations} object.
 * Typically, this is done by instantiating an anonymous subclass containing an instance initialization body, or as we
 * call it, an <em>expectation block</em>:
 * <pre>
 * // <em>Record</em> one or more expectations on available mocked types/instances.
 * new Expectations() {{
 *    <strong>mock1</strong>.expectedMethod(anyInt); result = 123; times = 2;
 *    <strong>mock2</strong>.anotherExpectedMethod(1, "test"); result = new String[] {"Abc", "xyz"};
 * }};
 *
 * // Exercise tested code, with previously recorded expectations now available for <em>replay</em>.
 * codeUnderTest.doSomething();
 * </pre>
 * It is also possible to create <em>named</em> subclasses.
 * <p/>
 * During replay, invocations matching a recorded expectation must occur at least <em>once</em> (unless specified
 * otherwise);
 * if, by the end of the test, no matching invocation occurred for a given recorded expectation, the test will fail with
 * a {@code MissingInvocation} error.
 * <p/>
 * When multiple expectations are recorded, matching invocations are allowed to occur in a <em>different</em> order.
 * So, the order in which expectations are recorded is not significant.
 * <p/>
 * Besides the special {@link #result} field already mentioned, there are several other fields and methods which can be
 * used inside the expectation block:
 * a) {@link #returns(Object, Object...)}, a convenience method for returning a <em>sequence</em> of values;
 * b) argument matchers such as {@link #anyInt}, {@link #anyString}, {@link #withNotNull()}, etc., which relax or
 * constrain the matching of argument values;
 * c) the {@link #times}, {@link #minTimes}, and {@link #maxTimes} fields, which relax or constrain the expected and/or
 * allowed number of matching invocations.
 * <p/>
 * By default, the exact instance on which instance method invocations will occur during replay is <em>not</em> verified
 * to be the same as the instance used when recording the expectation.
 * That said, instance-specific matching can be obtained by declaring the mocked type as
 * {@linkplain Injectable @Injectable}, by using the {@link #onInstance(Object)} method, or by declaring multiple mock
 * fields and/or mock parameters of the same mocked type (so that separate expectations can be recorded for each mock
 * instance).
 * <p/>
 * Invocations occurring during replay, whether they matched recorded expectations or not, can be explicitly verified
 * <em>after</em> exercising the code under test.
 * To that end, we use a set of complementary base classes: {@link Verifications}, {@link VerificationsInOrder},
 * {@link FullVerifications}, and {@link FullVerificationsInOrder}.
 * Similar to expectation blocks, these classes allow us to create <em>verification</em> blocks.
 * <p/>
 * Finally, note that this class has a specialized subclass: {@link StrictExpectations}.
 * That one differs in that 1) each recorded expectation has, by default, a maximum allowed number of invocations
 * of {@code 1} (one); 2) matching invocations must occur in the <em>same order</em> as recorded; 3) <em>only</em>
 * matching invocations are allowed, with any occurrence of unmatched invocations causing the test to fail with an
 * {@code UnexpectedInvocation} error; and 4) use of verification blocks is not supported, since all invocations are
 * implicitly verified through the strictly recorded sequence of expectations.
 * Most tests can and should only use {@code Expectations}; {@code StrictExpectations} are meant for users who prefer a
 * mocking style where all expectations are specified before exercising the code under test, or for situations where
 * the recording of a strict sequence of expectations produces a test that is more succinct or that fails as early as
 * an unexpected invocation occurs.
 *
 * @see #Expectations()
 * @see #Expectations(Object...)
 * @see #Expectations(Integer, Object...)
 * @see <a href="http://jmockit.org/tutorial/Mocking.html#expectation">Tutorial</a>
 */
public abstract class Expectations extends Invocations
{
   @Nonnull private final RecordAndReplayExecution execution;

   /**
    * A value assigned to this field will be taken as the result for the expectation that is being recorded.
    * <p/>
    * If the value is a {@link Throwable} then it will be <em>thrown</em> when a matching invocation later occurs.
    * Otherwise, it's assumed to be a <em>return value</em> for a non-<code>void</code> method, and will be returned
    * from a matching invocation.
    * <p/>
    * If no result is recorded for a given expectation, then all matching invocations will return the appropriate
    * default value according to the method return type:
    * <ul>
    * <li>Most {@code java.lang} types (<code>String</code>, {@code Object}, etc.): returns {@code null}.</li>
    * <li>{@code java.math} types (<code>BigDecimal</code>, etc.): returns {@code null}.</li>
    * <li>Primitive/wrapper types: returns the standard default value (<code>false</code> for {@code boolean/Boolean},
    * {@code 0} for {@code int/Integer}, and so on).
    * </li>
    * <li>{@code java.util.List}, {@code java.util.Collection}, or {@code java.lang.Iterable}: returns
    * {@link Collections#EMPTY_LIST}.</li>
    * <li>{@code java.util.Iterator} or {@code java.util.ListIterator}: returns an empty iterator.</li>
    * <li>{@code java.util.Set}: returns {@link Collections#EMPTY_SET}.</li>
    * <li>{@code java.util.SortedSet}: returns an unmodifiable empty sorted set.</li>
    * <li>{@code java.util.Map}: returns {@link Collections#EMPTY_MAP}.</li>
    * <li>{@code java.util.SortedMap}: returns an unmodifiable empty sorted map.</li>
    * <li>{@code java.util.Optional}: returns {@link Optional#empty()}.</li>
    * <li>Other reference types: returns a mocked instance through cascading.</li>
    * <li>Array types: returns an array with zero elements (empty) in each dimension.</li>
    * </ul>
    * <p/>
    * When an expectation is recorded for a method which actually <em>returns</em> an exception or error (as opposed to
    * <em>throwing</em> one), then the {@link #returns(Object, Object...)} method should be used instead, as it only
    * applies to return values.
    * <p/>
    * Assigning a value whose type differs from the method return type will cause an {@code IllegalArgumentException} to
    * be thrown, unless it can be safely converted to the return type.
    * One such conversion is from an array to a collection or iterator.
    * Another is from an array of at least two dimensions to a map, with the first dimension providing the keys and the
    * second the values.
    * Yet another conversion is from a single value to a container type holding that value.
    * <p/>
    * A sequence of <em>consecutive results</em> can be recorded simply by assigning the field multiple times for the
    * same expectation.
    * Alternatively, the desired sequence of results for a single-valued return type can be recorded by assigning an
    * array, an {@link Iterable}, or an {@link Iterator} containing the individual results in order.
    * <p/>
    * Results that depend on some programming logic can be provided through a {@linkplain Delegate} object assigned to
    * the field.
    * This applies to {@code void} and non-<code>void</code> methods, as well as to constructors.
    * <p/>
    * Finally, when recording an expectation on a <em>constructor</em> of a mocked class, an arbitrary instance of said
    * class can be assigned to the field.
    * In this case, the assigned instance will be used as a "replacement" for all invocations to
    * <em>instance methods</em> made on <em>other</em> instances, provided they get created sometime later through a
    * matching constructor invocation.
    *
    * @see #returns(Object, Object...)
    * @see <a href="http://jmockit.org/tutorial/Mocking.html#results">Tutorial</a>
    */
   protected Object result;

   /**
    * Registers one or more expectations recorded on available mocked types and/or mocked instances, as written inside
    * the instance initialization body of an anonymous subclass or the called constructor of a named subclass.
    *
    * @see #Expectations(Object...)
    * @see #Expectations(Integer, Object...)
    */
   protected Expectations()
   {
      execution = new RecordAndReplayExecution(this, (Object[]) null);
   }

   /**
    * Same as {@link #Expectations()}, except that one or more classes will be partially mocked according to the
    * expectations recorded in the expectation block.
    * <p/>
    * The classes to be partially mocked are those directly specified through their {@code Class} objects as well as
    * those to which any given objects belong.
    * During replay, any invocations to one of these classes or objects will execute real production code, unless a
    * matching expectation was recorded.
    * This mechanism, however, does not apply to {@code native} methods, which are not supported for partial mocking.
    * <p/>
    * For a given {@code Class} object, all constructors and methods can be mocked, from the specified class up to but
    * not including {@code java.lang.Object}.
    * For a given <em>object</em>, only methods can be mocked, not constructors; also, during replay, invocations to
    * instance methods will only match expectations recorded on the given instance (or instances, if more than one was
    * given).
    *
    * @param classesOrObjectsToBePartiallyMocked one or more classes or objects whose classes are to be partially mocked
    *
    * @throws IllegalArgumentException if given a class literal for an interface, an annotation, an array, a
    * primitive/wrapper type, or a {@linkplain java.lang.reflect.Proxy#isProxyClass(Class) proxy class} created for an
    * interface, or if given a value/instance of such a type
    * 
    * @see #Expectations(Integer, Object...)
    * @see <a href="http://jmockit.org/tutorial/Mocking.html#partial">Tutorial</a>
    */
   protected Expectations(Object... classesOrObjectsToBePartiallyMocked)
   {
      execution = new RecordAndReplayExecution(this, classesOrObjectsToBePartiallyMocked);
   }

   /**
    * Same as {@link #Expectations(Object...)}, but considering that the invocations inside the block will occur in a
    * given number of iterations.
    * <p/>
    * The effect of specifying a number of iterations larger than 1 (one) is equivalent to multiplying by that number
    * the lower and upper invocation count limits for each invocation inside the expectation block.
    * <p/>
    * It's also valid to have multiple expectation blocks for the same test, each with an arbitrary number of
    * iterations.
    *
    * @param numberOfIterations the positive number of iterations for the whole set of expectations recorded inside the
    * block; when not specified, 1 (one) iteration is assumed
    * @param classesOrObjectsToBePartiallyMocked one or more classes or objects whose classes are to be partially mocked
    *
    * @see #Expectations()
    */
   protected Expectations(Integer numberOfIterations, Object... classesOrObjectsToBePartiallyMocked)
   {
      this(classesOrObjectsToBePartiallyMocked);
      getCurrentPhase().setNumberOfIterations(numberOfIterations);
   }

   @Nonnull @Override
   final RecordPhase getCurrentPhase() { return execution.getRecordPhase(); }

   /**
    * Specifies that the previously recorded method invocation will return a given sequence of values during replay.
    * <p/>
    * Calling this method is equivalent to assigning the {@link #result} field two or more times in sequence, or
    * assigning it a single time with an array or iterable containing the same sequence of values.
    * <p/>
    * Certain data conversions will be applied, depending on the return type of the recorded method:
    * <ol>
    * <li>If the return type is iterable and can receive a {@link List} value, then the given sequence of values will be
    * converted into an {@code ArrayList}; this list will then be returned by matching invocations at replay time.</li>
    * <li>If the return type is {@code SortedSet} or a sub-type, then the given sequence of values will be converted
    * into a {@code TreeSet}; otherwise, if it is {@code Set} or a sub-type, then a {@code LinkedHashSet} will be
    * created to hold the values; the set will then be returned by matching invocations at replay time.</li>
    * <li>If the return type is {@code Iterator} or a sub-type, then the given sequence of values will be converted into
    * a {@code List} and the iterator created from this list will be returned by matching invocations at replay
    * time.</li>
    * <li>If the return type is an array, then the given sequence of values will be converted to an array of the same
    * type, which will be returned by matching invocations at replay time.</li>
    * </ol>
    * The current expectation will have its upper invocation count automatically set to the total number of values
    * specified to be returned.
    * This upper limit can be overridden through the {@code maxTimes} field, if necessary.
    * <p/>
    * If this method is used for a constructor or {@code void} method, the given return values will be ignored,
    * but matching invocations will be allowed during replay; they will simply do nothing.
    *
    * @param firstValue the first value to be returned at replay time
    * @param remainingValues the remaining values to be returned, in the same order
    *
    * @see <a href="http://jmockit.org/tutorial/Mocking.html#results">Tutorial</a>
    */
   protected final void returns(Object firstValue, Object... remainingValues)
   {
      getCurrentPhase().addSequenceOfReturnValues(firstValue, remainingValues);
   }
}
