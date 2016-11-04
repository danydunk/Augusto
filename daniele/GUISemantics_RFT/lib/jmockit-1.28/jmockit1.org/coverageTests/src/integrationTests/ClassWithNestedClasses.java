package integrationTests;

public class ClassWithNestedClasses
{
   public static class NestedClass
   {
      private final int i;

      public NestedClass()
      {
         i = 123;
      }

      private static final class DeeplyNestedClass
      {
         void print(String text) { System.out.println(text); }
      }

      private final class InnerClass
      {
         void print(String text) { System.out.println(text + ": " + i); }
      }
   }

   public static void doSomething()
   {
      new NestedClass.DeeplyNestedClass().print("test");

      // Just so we have two paths:
      if (System.out != null) {
         System.out.println("Test");
      }
   }

   public static boolean methodContainingAnonymousClass(int i)
   {
      new Cloneable() {};
      return i > 0;
   }
}