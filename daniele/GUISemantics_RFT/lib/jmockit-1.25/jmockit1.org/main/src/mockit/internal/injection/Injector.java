/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import javax.annotation.*;

interface Injector
{
   void fillOutDependenciesRecursively(@Nonnull Object dependency);
}
