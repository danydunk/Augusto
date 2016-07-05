package otherTests.serviceA;

import java.util.*;

import otherTests.serviceB.*;

public class ServiceA
{
   private String config;

   ServiceA(String config) { this.config = config; }
   public ServiceA() {}

   public String getConfig() { return config; }

   public boolean doSomethingThatUsesServiceB(int a, String b)
   {
      int x = new ServiceB(b).computeX(a, 5);

      return x > a;
   }

   public void doSomethingElseUsingServiceB(int noOfCallsToServiceB)
   {
      ServiceB serviceB = new ServiceB("config");

      for (int i = 0; i < noOfCallsToServiceB; i++) {
         serviceB.computeX(i, 1);
      }

      config = serviceB.getConfig();
   }

   public String performComplexOperation(List<?> items)
   {
      ServiceB serviceB = new ServiceB(config);
      int i = 1;

      for (Object item : items) {
         serviceB.computeX(i, item.hashCode());
      }

      return serviceB.findItem("ABC", "xyz", "01");
   }
}
