package com.truward.brikar.sample.rpcExplorerDemo.controller;

import com.truward.brikar.rpc.explorer.ExplorerControllerSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/g")
public final class UserServiceExplorerController extends ExplorerControllerSupport {

  @RequestMapping(value = "/explorer", method = RequestMethod.GET)
  public void explorer(HttpServletResponse response) throws IOException {
    super.explorer(response);
  }

  @Override
  protected String getServiceName() {
    return "user-service";
  }
}
