/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import javax.swing.*;

import org.junit.*;

import static org.junit.Assert.*;

public final class CovariantReturnTypesTest
{
   public static class SuperClass { public JTextField getTextField() { return null; } }

   public static final class SubClass extends SuperClass
   {
      @Override public JPasswordField getTextField() { return null; }
   }

   @Test
   public void methodInClassHierarchyUsingStrictExpectations(@Mocked final SubClass mock)
   {
      final JPasswordField passwordField = new JPasswordField();

      new StrictExpectations() {{
         // These recordings apply to all calls to the method on a SubClass object, regardless of
         // whether the owner object is of a base or derived type.
         mock.getTextField(); result = passwordField;
         mock.getTextField(); result = passwordField;
      }};

      assertSame(passwordField, mock.getTextField());
      assertSame(passwordField, ((SuperClass) mock).getTextField());
   }

   @Test
   public void methodInClassHierarchyUsingNotStrictExpectations(@Mocked final SubClass mock)
   {
      final JPasswordField passwordField = new JPasswordField();

      new Expectations() {{
         mock.getTextField();
         result = passwordField;
      }};

      SubClass subClassInstance = new SubClass();
      assertSame(passwordField, subClassInstance.getTextField());
      assertSame(passwordField, ((SuperClass) subClassInstance).getTextField());
   }

   public abstract static class AbstractBaseClass
   {
      protected AbstractBaseClass() {}
      public abstract JTextField getTextField();
   }

   public static class ConcreteClass extends AbstractBaseClass
   {
      @Override public JFormattedTextField getTextField() { return null; }
   }

   @Test
   public void concreteMethodImplementationUsingStrictExpectations(@Mocked final ConcreteClass mock)
   {
      final JTextField formattedField = new JFormattedTextField();

      new StrictExpectations() {{
         mock.getTextField(); result = formattedField;
         ((AbstractBaseClass) mock).getTextField(); result = formattedField;
      }};

      assertSame(formattedField, mock.getTextField());
      assertSame(formattedField, ((AbstractBaseClass) mock).getTextField());
   }

   @Test
   public void concreteMethodImplementationUsingNotStrictExpectations(@Mocked final ConcreteClass mock)
   {
      final JTextField formattedField1 = new JFormattedTextField();
      final JTextField formattedField2 = new JFormattedTextField();

      new Expectations() {{
         mock.getTextField(); returns(formattedField1, formattedField2);
      }};

      assertSame(formattedField1, mock.getTextField());
      assertSame(formattedField2, ((AbstractBaseClass) mock).getTextField());
   }

   @Test
   public void abstractMethodImplementationUsingStrictExpectations(@Capturing final AbstractBaseClass mock)
   {
      final JTextField regularField = new JTextField();
      final JTextField formattedField = new JFormattedTextField();

      new StrictExpectations() {{
         mock.getTextField(); result = regularField;
         mock.getTextField(); result = formattedField;
      }};

      AbstractBaseClass firstInstance = new AbstractBaseClass() {
         @Override public JTextField getTextField() { return null; }
      };
      assertSame(regularField, firstInstance.getTextField());

      assertSame(formattedField, firstInstance.getTextField());
   }

   @Test
   public void abstractMethodImplementationUsingNotStrictExpectations(@Capturing final AbstractBaseClass mock)
   {
      final JTextField regularField = new JTextField();
      final JTextField formattedField = new JFormattedTextField();

      new Expectations() {{
         mock.getTextField(); result = new JTextField[] {regularField, formattedField};
      }};

      AbstractBaseClass firstInstance = new AbstractBaseClass() {
         @Override public JTextField getTextField() { return null; }
      };
      assertSame(regularField, firstInstance.getTextField());

      assertSame(formattedField, firstInstance.getTextField());
   }

   public interface SuperInterface { void someOtherMethod(int i); Object getValue(); }
   public interface SubInterface extends SuperInterface { @Override String getValue(); }

   @Test
   public void methodInSuperInterfaceWithVaryingReturnValuesUsingStrictExpectations(@Mocked final SuperInterface mock)
   {
      final Object value = new Object();
      final String specificValue = "test";

      new StrictExpectations() {{
         mock.getValue(); result = value;
         mock.getValue(); result = specificValue;
      }};

      assertSame(value, mock.getValue());
      assertSame(specificValue, mock.getValue());
   }

   @Test
   public void methodInSuperInterfaceWithVaryingReturnValuesUsingNotStrictExpectations(
      @Mocked final SuperInterface mock)
   {
      final Object value = new Object();
      final String specificValue = "test";

      new Expectations() {{
         mock.getValue(); result = value; result = specificValue;
      }};

      assertSame(value, mock.getValue());
      assertSame(specificValue, mock.getValue());
   }

   @Test
   public void methodInSubInterfaceUsingStrictExpectations(@Mocked final SubInterface mock)
   {
      @SuppressWarnings("UnnecessaryLocalVariable") final SuperInterface base = mock;
      final Object value = new Object();
      final String specificValue = "test";

      new StrictExpectations() {{
         base.getValue(); result = value;
         base.getValue(); result = specificValue;

         mock.someOtherMethod(anyInt);

         mock.getValue(); result = specificValue;
         base.getValue(); result = specificValue;
      }};

      assertSame(value, base.getValue());
      assertSame(specificValue, base.getValue());

      mock.someOtherMethod(1);

      assertSame(specificValue, mock.getValue());
      assertSame(specificValue, base.getValue());
   }

   @Test
   public void methodInSubInterfaceUsingNotStrictExpectations(@Mocked final SubInterface mock)
   {
      @SuppressWarnings("UnnecessaryLocalVariable") final SuperInterface base = mock;
      final Object value = new Object();
      final String specificValue1 = "test1";
      final String specificValue2 = "test2";

      new Expectations() {{
         base.getValue(); returns(specificValue1, value);

         mock.getValue(); result = specificValue2;
      }};

      assertSame(specificValue1, base.getValue());
      assertSame(value, base.getValue());

      assertSame(specificValue2, mock.getValue());
   }

   @Test
   public void methodInSubInterfaceReplayedThroughSuperInterfaceUsingStrictExpectations(@Mocked final SubInterface mock)
   {
      final String specificValue = "test";

      new StrictExpectations() {{ mock.getValue(); result = specificValue; }};

      assertSame(specificValue, ((SuperInterface) mock).getValue());
   }

   @Test
   public void methodInSubInterfaceReplayedThroughSuperInterfaceUsingNotStrictExpectations(
      @Mocked final SubInterface mock)
   {
      final String specificValue = "test";

      new Expectations() {{ mock.getValue(); result = specificValue; }};

      assertSame(specificValue, ((SuperInterface) mock).getValue());
   }
}
