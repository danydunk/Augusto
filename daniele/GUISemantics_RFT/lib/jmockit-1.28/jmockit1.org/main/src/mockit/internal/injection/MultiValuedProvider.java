/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.annotation.*;

import static mockit.internal.util.Utilities.getClassType;

final class MultiValuedProvider implements InjectionPointProvider
{
   @Nonnull private final Type declaredType;
   @Nonnull private final List<InjectionPointProvider> individualProviders;

   MultiValuedProvider(@Nonnull Type elementType)
   {
      declaredType = elementType;
      individualProviders = new ArrayList<InjectionPointProvider>();
   }

   void addInjectable(@Nonnull InjectionPointProvider provider)
   {
      individualProviders.add(provider);
   }

   @Nonnull @Override
   public Type getDeclaredType() { return declaredType; }

   @Nonnull @Override
   public Class<?> getClassOfDeclaredType() { return getClassType(declaredType); }

   @Nonnull @Override
   public String getName() { throw new UnsupportedOperationException("No name"); }

   @Nonnull @Override
   public Annotation[] getAnnotations() { throw new UnsupportedOperationException("No annotations"); }

   @Nullable @Override
   public Object getValue(@Nullable Object owner)
   {
      List<Object> values = new ArrayList<Object>(individualProviders.size());

      for (InjectionPointProvider provider : individualProviders) {
         Object value = provider.getValue(owner);
         values.add(value);
      }

      return values;
   }
}
