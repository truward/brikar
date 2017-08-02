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
    final StringBuilder builder = new StringBuilder(10 + metricsList.size() * 100);
    builder.append(LogUtil.METRIC_ENTRY);

    boolean next = false;
    for (final Metrics metrics : metricsList) {
      if (next) {
        builder.append(System.lineSeparator()).append('\t');
      } else {
        builder.append(' ');
        next = true;
      }

      try {
        metrics.appendTo(builder);
      } catch (IOException e) {
        // suppress error - should never happen
        builder.append("Internal Error: ").append(e.getMessage());
      }
    }

    return builder.toString();
  }
}
