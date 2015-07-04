package com.truward.brikar.server.healthcheck;

import com.truward.brikar.server.controller.AbstractRestController;
import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest")
public class SimpleHealthCheckRestController extends AbstractRestController implements HealthCheckRestService {

  @Override
  public final String checkHealth() {
    doCheckHealth();
    return HealthCheckRestService.OK;
  }

  protected void doCheckHealth() {
    // do nothing
  }
}
