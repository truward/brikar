package com.truward.brikar.server.test.args;

import com.truward.brikar.server.args.StandardArgParser;
import com.truward.brikar.server.args.StartArgs;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Shabanov
 */
public final class StandardArgParserTest {

  @Test
  public void shouldParseArgs() {
    // Given:
    final String[] args = { "--port", "5555", "--config", "cfgfile" };
    final StandardArgParser argParser = new StandardArgParser(args);

    // When:
    final int result = argParser.parse();

    // Then:
    assertEquals(0, result);
    assertTrue(argParser.isReadyToStart());

    final StartArgs startArgs = argParser.getStartArgs();
    assertEquals(5555, startArgs.getPort());
    assertEquals("cfgfile", startArgs.getConfigPath());
  }
}
