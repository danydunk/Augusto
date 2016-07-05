/*
 * Copyright (c) 2006 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import javax.annotation.*;

import mockit.coverage.data.*;

public final class BasicCoverageReport extends CoverageReport
{
   public BasicCoverageReport(
      @Nonnull String outputDir, boolean outputDirCreated, @Nullable String[] sourceDirs,
      @Nonnull CoverageData coverageData)
   {
      super(outputDir, outputDirCreated, sourceDirs, coverageData, false);
   }
}
