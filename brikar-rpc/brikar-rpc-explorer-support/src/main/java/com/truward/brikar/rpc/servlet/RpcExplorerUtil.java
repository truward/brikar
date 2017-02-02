package com.truward.brikar.rpc.servlet;

import com.truward.brikar.rpc.RpcMethod;
import com.truward.brikar.rpc.ServletRpcBinding;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Utility class for serving explorer pages.
 */
public final class RpcExplorerUtil {
  private RpcExplorerUtil() {} // Hidden

  private static final String STATE_INITIALIZER_MARKER = "// State Initializer";

  public static void serveFromFile(String filePath,
                                   ServletRpcBinding rpcBinding,
                                   HttpServletResponse response) throws IOException {
    final File file = new File(filePath);
    if (!file.exists()) {
      LoggerFactory.getLogger(RpcExplorerUtil.class).error("File doesn't exist");
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }


    final byte[] fileByteContents = Files.readAllBytes(file.toPath());
    final String template = new String(fileByteContents, StandardCharsets.UTF_8);

    final int initializerMarkerIndex = template.indexOf(STATE_INITIALIZER_MARKER);
    if (initializerMarkerIndex < 0) {
      LoggerFactory.getLogger(RpcExplorerUtil.class).error("No initializer marker in a given file");
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    serveTemplate(rpcBinding, template, initializerMarkerIndex, response);
  }

  public static void serveTemplate(ServletRpcBinding rpcBinding,
                                   String template, int templateInitializerIndex,
                                   HttpServletResponse response) throws IOException {
    try (final Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
      // start of template
      writer.append(template, 0, templateInitializerIndex);

      // inject state
      writer.append("state = {\nmethods: {\n");

      for (final RpcMethod method : rpcBinding.getExposedMethods()) {
        writer.append(method.getName()).append(": {\n");
        writer.append("templateRequest: ");
        writer.append("'{}'"); // TODO: real prototype
        writer.append('\n'); // end of templateRequest
        writer.append("},\n"); // end of method
      }
      writer.append("},\n"); // end of 'methods'
      writer.append("serviceName: '").append(rpcBinding.getServiceName()).append("',\n");
      writer.append("generated: ").append(Long.toString(System.currentTimeMillis())).append('\n');

      writer.append('}'); // end of state declaration

      // end of template
      writer.append(template, templateInitializerIndex + STATE_INITIALIZER_MARKER.length(), template.length());
    }

    response.setHeader("Content-Type", "text/html");
  }
}
