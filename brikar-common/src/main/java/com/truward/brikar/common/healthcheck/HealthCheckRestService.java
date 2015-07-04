package com.truward.brikar.common.healthcheck;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Service for performing basic health checks.
 * The server will be considered healthy as long as the returned string equals to OK.
 *
 * @author Alexander Shabanov
 */
public interface HealthCheckRestService {

  /**
   * String, that {@link #checkHealth()} should return in order to indicate that server is healthy.
   */
  String OK = "OK";

  @RequestMapping(value = "/health", method = RequestMethod.POST)
  @ResponseBody
  String checkHealth();
}
