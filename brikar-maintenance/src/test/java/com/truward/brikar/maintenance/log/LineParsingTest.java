package com.truward.brikar.maintenance.log;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.maintenance.log.message.LogMessage;
import com.truward.brikar.maintenance.log.message.NullLogMessage;
import com.truward.brikar.maintenance.log.processor.LogMessageProcessor;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LineParsingTest {

  private static final String MSG1 = "2015-07-24 23:21:16,942 INFO learn.LogProducerMain " +
          "oid=aJ0JLwgnBlw7+tbZ, rid=8tYCTFqDZfXJEzgD " +
          "[qtp1965409981-14] Application started with args=[]";

  private static final String MSG2 = "2015-07-24 23:22:20,748 WARN learn.LogProducerMain  " +
          "[learn.LogProducerMain.main()] Operation timed out";

  private static final String MSG3 = "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
          "rid=KhnHxNK/BbLbaiH4 " +
          "[learn.LogProducerMain.main()] Disk full";

  private static final String MSG4 = "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
          "rid=1, oid=2, lc=3 " +
          "[learn.LogProducerMain.main()] Disk full";

  private static final String MSG5 = "2015-07-24 23:22:20,748 INFO learn.LogProducerMain " +
      "rid=KhnHxNK/BbLbaiH4 " +
      "[learn.LogProducerMain.main()] @metric1 tDelta=545, op=UserService.getUserById";

  private final LogMessageProcessor processor = new LogMessageProcessor();

  @Test
  public void shouldMatchMsg1() {
    final LogMessage logMessage = processor.parse(MSG1, NullLogMessage.INSTANCE);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(MSG1, logMessage.getLogEntry());
    assertEquals(1437780076942L, logMessage.getUnixTime());
    assertEquals(Collections.singletonList(MSG1), logMessage.getLines());
    assertEquals(2, logMessage.getAttributes().size());
    assertEquals("aJ0JLwgnBlw7+tbZ", logMessage.getAttributes().get("oid"));
    assertEquals("8tYCTFqDZfXJEzgD", logMessage.getAttributes().get("rid"));
  }

  @Test
  public void shouldMatchMsg2() {
    final LogMessage logMessage = processor.parse(MSG2, NullLogMessage.INSTANCE);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.WARN, logMessage.getSeverity());
    assertEquals(MSG2, logMessage.getLogEntry());
    assertTrue(logMessage.getAttributes().isEmpty());
  }

  @Test
  public void shouldMatchMsg3() {
    final LogMessage logMessage = processor.parse(MSG3, NullLogMessage.INSTANCE);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.ERROR, logMessage.getSeverity());
    assertEquals(MSG3, logMessage.getLogEntry());
    assertEquals(null, logMessage.getAttributes().get("oid"));
    assertEquals("KhnHxNK/BbLbaiH4", logMessage.getAttributes().get("rid"));
  }

  @Test
  public void shouldMatchMsg4() {
    final LogMessage logMessage = processor.parse(MSG4, NullLogMessage.INSTANCE);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.ERROR, logMessage.getSeverity());
    assertEquals(MSG4, logMessage.getLogEntry());
    assertEquals("1", logMessage.getAttributes().get("rid"));
    assertEquals("2", logMessage.getAttributes().get("oid"));
    assertEquals("3", logMessage.getAttributes().get("lc"));
  }

  @Test
  public void shouldMatchMsg5() {
    final LogMessage logMessage = processor.parse(MSG5, NullLogMessage.INSTANCE);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(MSG5, logMessage.getLogEntry());

    assertEquals("UserService.getUserById", logMessage.getAttributes().get(LogUtil.OPERATION));
    assertEquals(545L, logMessage.getAttributes().get(LogUtil.TIME_DELTA));
    assertEquals("KhnHxNK/BbLbaiH4", logMessage.getAttributes().get("rid"));
  }

  @Test
  public void shouldMatchMsg5ChainedWithExtraOne() {
    final LogMessage prevLogMessage = processor.parse(MSG5, NullLogMessage.INSTANCE);

    final String anotherLine = "\top=Another, tStart=1000, tDelta=20";
    final LogMessage logMessage = processor.parse(anotherLine, prevLogMessage);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(Collections.singletonList(anotherLine), logMessage.getLines());

    assertEquals("Another", logMessage.getAttributes().get(LogUtil.OPERATION));
    assertEquals(20L, logMessage.getAttributes().get(LogUtil.TIME_DELTA));
    assertEquals(1000L, logMessage.getAttributes().get(LogUtil.START_TIME));
  }
}
