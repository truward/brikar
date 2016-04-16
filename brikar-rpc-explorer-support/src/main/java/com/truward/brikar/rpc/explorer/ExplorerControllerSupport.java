package com.truward.brikar.rpc.explorer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Controller for RPC service.
 *
 * @author Alexander Shabanov
 */
public abstract class ExplorerControllerSupport {

  @RequestMapping(value = "/explorer", method = RequestMethod.GET)
  public void explorer(HttpServletResponse response) throws IOException {
    try (final Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
      writeHeader(writer);

      writer.append(
          "<div class=\"container\">" +
              "<h2>Service Explorer</h2>\n" +
              "<div class=\"well\">" +
              "<h2>Test</h2>" +
              "<p>Lorem ipsum</p>" +
              "</div>" +
              "</div>"
      );

      writeFooter(writer);
    }
  }

  //
  // Abstract
  //

  protected abstract String getServiceName();

  //
  // Protected
  //

  protected void writeHeader(Writer writer) throws IOException {
    // write first half of html header
    writer.append(
        "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta http-equiv='Content-type' content='text/html; charset=utf-8'>\n"
    );

    // write title
    writer.append("<title>").append(getServiceName()).append("</title>\n");

    // write second half of html header
    writer.append(
        "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" " +
            "integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\"/>\n" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css\" " +
            "integrity=\"sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r\" crossorigin=\"anonymous\"/>\n" +

            "</head>\n" +
            "<body>\n"
    );
  }

  protected void writeFooter(Writer writer) throws IOException {
    writer.append(
        "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.2.2/jquery.min.js\"></script>\n" +
            "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\" " +
            "integrity=\"sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS\" " +
            "crossorigin=\"anonymous\"></script>\n" +
            "</body>" +
            "</html>"
    );
  }
}
