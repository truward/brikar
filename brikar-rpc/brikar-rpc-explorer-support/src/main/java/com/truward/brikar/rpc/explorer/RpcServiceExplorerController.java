package com.truward.brikar.rpc.explorer;

import com.truward.brikar.rpc.ServletRpcBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.truward.brikar.rpc.servlet.RpcExplorerUtil.serveFromFile;
import static com.truward.brikar.rpc.servlet.RpcExplorerUtil.serveTemplate;

/**
 * Controller for RPC service.
 * Writes inline HTML to avoid depending on any templating engine.
 *
 * @author Alexander Shabanov
 */
@Controller
public class RpcServiceExplorerController {

  public static final String TEMPLATE_FILE_PATH_PROPERTY = "brikar.explorer.templatePath";

  private static final String STATE_INITIALIZER_MARKER = "// State Initializer";

  private Map<String, ServletRpcBinding> rpcBindingMap = Collections.emptyMap();

  private String filePath;

  private String template;
  private int templateInitializerIndex;
  private boolean serviceExplorerEnabled;

  public RpcServiceExplorerController() {
    this.filePath = System.getProperty(TEMPLATE_FILE_PATH_PROPERTY);
    if (filePath != null) {
      return;
    }

    // load template from resource
    final InputStream is = getClass().getClassLoader().getResourceAsStream("com/truward/brikar/rpc/explorer/RpcServiceExplorerController-template.html");
    if (is == null) {
      throw new IllegalStateException("Default template does not exist");
    }

    try {
      loadTemplate(StreamUtils.copyToString(is, StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    setServiceExplorerEnabled(true);
  }

  public void setServiceExplorerEnabled(boolean serviceExplorerEnabled) {
    this.serviceExplorerEnabled = serviceExplorerEnabled;
  }

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

  @RequestMapping(value = "api/rpc/{serviceName}/{methodName}", method = RequestMethod.POST)
  public void invoke(@PathVariable("serviceName") String serviceName,
                     @PathVariable("methodName") String methodName,
                     HttpServletRequest request,
                     HttpServletResponse response) throws IOException {
    final ServletRpcBinding rpcBinding = rpcBindingMap.get(serviceName);
    if (rpcBinding == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    boolean succeeded = false;
    try {
      rpcBinding.process(methodName, request, response);
      succeeded = true;
    } finally {
      if (!succeeded) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  @RequestMapping(value = "api/explorer/{serviceName}", method = RequestMethod.GET)
  public void explorer(@PathVariable("serviceName") String serviceName,
                       HttpServletResponse response) throws IOException {
    if (!serviceExplorerEnabled) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Explorer disabled");
      return;
    }

    final ServletRpcBinding rpcBinding = rpcBindingMap.get(serviceName);
    if (rpcBinding == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such service to explore");
      return;
    }

    if (filePath != null) {
      serveFromFile(filePath, rpcBinding, response);
      return;
    }

    serveTemplate(rpcBinding, template, templateInitializerIndex, response);
  }

  //
  // Private
  //

  private void loadTemplate(String template) {
    final int initializerMarkerIndex = template.indexOf(STATE_INITIALIZER_MARKER);
    if (initializerMarkerIndex < 0) {
      throw new IllegalStateException("There is no initializer marker in template");
    }

    this.template = template;
    this.templateInitializerIndex = initializerMarkerIndex;
  }
}
