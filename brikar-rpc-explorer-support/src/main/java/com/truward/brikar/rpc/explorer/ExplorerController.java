package com.truward.brikar.rpc.explorer;

import com.truward.brikar.rpc.ServletRpcBinding;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Controller for RPC service.
 * Writes inline HTML to avoid depending on any templating engine.
 *
 * TODO: use URLEncode whenever appropriate (e.g. serviceName)
 *
 * @author Alexander Shabanov
 */
@Controller
public class ExplorerController {
  private Map<String, ServletRpcBinding> rpcBindingMap = Collections.emptyMap();

  public void setRpcBinding(ServletRpcBinding rpcBinding) {
    setRpcBindings(Collections.singletonList(Objects.requireNonNull(rpcBinding, "rpcBinding")));
  }

  public void setRpcBindings(List<ServletRpcBinding> rpcBindings) {
    Objects.requireNonNull(rpcBindings, "rpcBindings");

    final Map<String, ServletRpcBinding> result = new HashMap<>(rpcBindings.size() * 2);

    for (int i = 0; i < rpcBindings.size(); ++i) {
      final ServletRpcBinding rpcBinding = rpcBindings.get(i);
      if (rpcBinding == null) {
        throw new IllegalArgumentException("rpcBinding #" + i  + " is null");
      }

      final String serviceName = rpcBinding.getServiceName();
      if (StringUtils.isEmpty(serviceName)) {
        throw new IllegalArgumentException("rpcBinding #" + i  + " has empty serviceName");
      }

      final ServletRpcBinding prevRpcBinding = result.put(serviceName, rpcBinding);
      if (prevRpcBinding != null) {
        throw new IllegalArgumentException("rpcBinding #" + i  + " has the same serviceName=" + serviceName +
            " as previous rpcBinding #" + rpcBindings.indexOf(prevRpcBinding));
      }
    }

    this.rpcBindingMap = result;
  }

  @RequestMapping(value = "rest/{serviceName}/explorer", method = RequestMethod.GET)
  public void explorer(@PathVariable("serviceName") String serviceName,
                       HttpServletResponse response) throws IOException {
    final ServletRpcBinding rpcBinding = rpcBindingMap.get(serviceName);
    if (rpcBinding == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    try (final Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
      writeHeader(serviceName, writer);
      writeBody(rpcBinding, writer);writeFooter(writer);
    }
  }

  //
  // Protected
  //

  protected void writeBody(ServletRpcBinding rpcBinding, Writer writer) throws IOException {
    writer.append("<div class=\"container\">");
    writer.append("<h2>").append(rpcBinding.getServiceName()).append(" Service Explorer").append("</h2>");

    writer.append("<div class=\"well\">" +
        "<h2>Test</h2>\n" +
        "<p>Lorem ipsum</p>\n");

    writer.append("<li>").append('\n'); // list
    for (final String methodName : rpcBinding.getExposedMethodNames()) {
      writer.append("<ul>").append(methodName).append("</ul>").append('\n');
    }
    writer.append("</li>").append('\n'); // list

    writer.append("</div>").append('\n'); // well

    writer.append("<p>Generated at ").append(String.valueOf(new Date())).append("</p>");

    writer.append("</div>").append('\n'); // container
  }

  protected void writeHeader(String serviceName, Writer writer) throws IOException {
    // write first half of html header
    writer.append(
        "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta http-equiv='Content-type' content='text/html; charset=utf-8'>\n"
    );

    // write title
    writer.append("<title>").append(serviceName).append(" Explorer</title>\n");

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
