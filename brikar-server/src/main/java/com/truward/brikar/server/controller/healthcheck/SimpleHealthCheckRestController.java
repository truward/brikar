package com.truward.brikar.server.controller.healthcheck;

import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import com.truward.brikar.server.controller.AbstractRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple implementation of health check that does nothing. It can be extended by overriding
 * {@link #doCheckHealth()} method.
 *
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest")
public class SimpleHealthCheckRestController extends AbstractRestController implements HealthCheckRestService {

  @Override
  public final String checkHealth() {
    return doCheckHealth();
  }

  protected String doCheckHealth() {
    return HealthCheckRestService.OK;
  }
}
