package com.truward.brikar.common.log.metric;

import com.truward.brikar.common.log.LogUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class StandardMetricsCollection implements MetricsCollection {
  private final List<Metrics> metricsList = new CopyOnWriteArrayList<>();

  @Override
  public void add(Metrics metrics) {
    metricsList.add(metrics);
  }

  @Override
  public String toString() {
    final Metrics[] metricsArray = metricsList.toArray(new Metrics[metricsList.size()]);
    final StringBuilder builder = new StringBuilder(10 + metricsArray.length * 100);
    builder.append(LogUtil.METRIC_ENTRY);

    for (int i = 0; i < metricsArray.length; ++i) {
      if (i == 0) {
        builder.append(' ');
      } else {
        builder.append(System.lineSeparator()).append('\t');
      }
      try {
        metricsArray[i].appendTo(builder);
      } catch (IOException e) {
        // suppress error - should never happen
        builder.append("<error>");
      }
    }

    return builder.toString();
  }
}
