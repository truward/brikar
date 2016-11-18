package com.truward.brikar.common.test.tracking;

import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import org.junit.Test;

import java.util.*;
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

    final TrackingHttpHeaderNames.RequestIdParts parts = TrackingHttpHeaderNames.parseRequestId(nestedRid);

    assertEquals("rid", parts.getRequestId());
    assertEquals(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L), parts.getSuffixes());
    assertEquals(nestedRid, parts.toString());
  }

  @Test
  public void shouldParseMaxValueSuffix() {
    final String rid = "rid";

    final String nestedRid = TrackingHttpHeaderNames.getNestedRequestId(rid, Long.MAX_VALUE);
    final TrackingHttpHeaderNames.RequestIdParts parts = TrackingHttpHeaderNames.parseRequestId(nestedRid);

    assertEquals(rid, parts.getRequestId());
    assertEquals(Collections.singletonList(Long.MAX_VALUE), parts.getSuffixes());
  }

  @Test
  public void shouldGenerateNestedRequestId() {
    final String rid = "req";

    final String nestedRid = TrackingHttpHeaderNames.getNestedRequestId(rid);

    assertNotSame(rid, nestedRid);
    final TrackingHttpHeaderNames.RequestIdParts parts = TrackingHttpHeaderNames.parseRequestId(nestedRid);
    assertEquals(rid, parts.getRequestId());
    assertEquals(1, parts.getSuffixes().size());
  }

  @Test
  public void shouldConvertSingleRequestId() {
    final String rid = "r";

    final TrackingHttpHeaderNames.RequestIdParts parts = TrackingHttpHeaderNames.parseRequestId(rid);

    assertEquals(rid, parts.getRequestId());
    assertEquals(Collections.emptyList(), parts.getSuffixes());
  }

  @Test
  public void shouldGenerateMultipleNestedRequestIds() {
    final String rid = "testRequestId";

    String nestedRid = rid;
    final List<Long> suffixes = new ArrayList<>();
    final ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < 100; ++i) {
      long suffix = random.nextLong(0, Long.MAX_VALUE);
      nestedRid = TrackingHttpHeaderNames.getNestedRequestId(nestedRid, suffix);
      suffixes.add(suffix);
    }

    final TrackingHttpHeaderNames.RequestIdParts parts = TrackingHttpHeaderNames.parseRequestId(nestedRid);
    assertEquals(new TrackingHttpHeaderNames.RequestIdParts(rid, suffixes), parts);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyRequestIdNesting() {
    TrackingHttpHeaderNames.getNestedRequestId("", 1L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMalformedRequestIdParsingWithTrailingDot() {
    TrackingHttpHeaderNames.parseRequestId("r.1.");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMalformedRequestIdParsingWithNeighbourDots() {
    TrackingHttpHeaderNames.parseRequestId("r.1..2");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectMalformedRequestIdParsingWithEmptyCore() {
    TrackingHttpHeaderNames.parseRequestId(".1.2");
  }
}
