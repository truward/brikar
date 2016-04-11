package com.truward.brikar.rpc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
public interface ServletRpcBinding {

  void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
