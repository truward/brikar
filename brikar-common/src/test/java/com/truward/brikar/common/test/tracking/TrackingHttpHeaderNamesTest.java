package com.truward.brikar.common.test.tracking;

import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Tests {@link TrackingHttpHeaderNames}.
 *
 * @author Alexander Shabanov
 */
public final class TrackingHttpHeaderNamesTest {

  @Test
  public void shouldParseManuallyConstructedNestedRid() {
    final String nestedRid = "rid.0.1.2.3.4.5.6.7";

    final TrackingHttpHeaderNames.RequestVectorParts parts = TrackingHttpHeaderNames.parseRequestVector(nestedRid);

    assertEquals("rid", parts.getRequestVector());
    assertEquals(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L), parts.getSuffixes());
    assertEquals(nestedRid, parts.toString());
  }

  @Test
  public void shouldParseMaxValueSuffix() {
    final String rid = "rid";

    final String nestedRid = TrackingHttpHeaderNames.getNestedRequestVector(rid, Long.MAX_VALUE);
    final TrackingHttpHeaderNames.RequestVectorParts parts = TrackingHttpHeaderNames.parseRequestVector(nestedRid);

    assertEquals(rid, parts.getRequestVector());
    assertEquals(Collections.singletonList(Long.MAX_VALUE), parts.getSuffixes());
  }

  @Test
  public void shouldGenerateNestedRequestVector() {
    final String rid = "req";

    final String nestedRid = TrackingHttpHeaderNames.getNestedRequestVector(rid);

    assertNotSame(rid, nestedRid);
    final TrackingHttpHeaderNames.RequestVectorParts parts = TrackingHttpHeaderNames.parseRequestVector(nestedRid);
    assertEquals(rid, parts.getRequestVector());
    assertEquals(1, parts.getSuffixes().size());
  }

  @Test
  public void shouldConvertSingleRequestVector() {
    final String rid = "r";

    final TrackingHttpHeaderNames.RequestVectorParts parts = TrackingHttpHeaderNames.parseRequestVector(rid);

    assertEquals(rid, parts.getRequestVector());
    assertEquals(Collections.emptyList(), parts.getSuffixes());
  }

  @Test
  public void shouldGenerateMultipleNestedRequestVectors() {
    final String rid = "testRequestVector";

    String nestedRid = rid;
    final List<Long> suffixes = new ArrayList<>();
    final ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < 100; ++i) {
      long suffix = random.nextLong(0, Long.MAX_VALUE);
      nestedRid = TrackingHttpHeaderNames.getNestedRequestVector(nestedRid, suffix);
      suffixes.add(suffix);
    }

    final TrackingHttpHeaderNames.RequestVectorParts parts = TrackingHttpHeaderNames.parseRequestVector(nestedRid);
    assertEquals(new TrackingHttpHeaderNames.RequestVectorParts(rid, suffixes), parts);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyRequestVectorNesting() {
    TrackingHttpHeaderNames.getNestedRequestVector("", 1L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMalformedRequestVectorParsingWithTrailingDot() {
    TrackingHttpHeaderNames.parseRequestVector("r.1.");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMalformedRequestVectorParsingWithNeighbourDots() {
    TrackingHttpHeaderNames.parseRequestVector("r.1..2");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMalformedRequestVectorParsingWithEmptyCore() {
    TrackingHttpHeaderNames.parseRequestVector(".1.2");
  }
}
