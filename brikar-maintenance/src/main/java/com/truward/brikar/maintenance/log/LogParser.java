package com.truward.brikar.maintenance.log;

import com.truward.brikar.maintenance.log.message.LogMessage;
import com.truward.brikar.maintenance.log.message.NullLogMessage;
import com.truward.brikar.maintenance.log.processor.LogMessageProcessor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public class LogParser {
  private final LogMessageProcessor logMessageProcessor = new LogMessageProcessor();

  public List<LogMessage> parse(InputStream inputStream) throws IOException {
    final List<LogMessage> messages = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        final LogMessage newMessage = logMessageProcessor.parse(line);
        final LogMessage oldMessage = messages.isEmpty() ? NullLogMessage.INSTANCE : messages.get(messages.size() - 1);

        if (newMessage.isNull()) {
          continue; // drop new log message (parse error?)
        }

        if (newMessage.isMultiLinePart()) {
          if (oldMessage.isMultiLinePart() || oldMessage.isNull()) {
            // old message is null or not present
            continue; // likely a malformed log
          }

          oldMessage.addLine(newMessage.getLogEntry());
          continue;
        }

        messages.add(newMessage);
      }
    }

    return messages;
  }

  public List<LogMessage> parse(File logFile) throws IOException {
    try (final FileInputStream fis = new FileInputStream(logFile)) {
      return parse(fis);
    }
  }
}
