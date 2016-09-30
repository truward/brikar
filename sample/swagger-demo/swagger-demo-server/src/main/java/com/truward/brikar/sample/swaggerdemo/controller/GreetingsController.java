package com.truward.brikar.sample.swaggerdemo.controller;

import com.truward.brikar.sample.swaggerdemo.model.GreetingModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/v1/greetings")
public final class GreetingsController {

  @RequestMapping("/greeting/{id}")
  @ResponseBody
  public GreetingModel.Greeting getGreeting(@PathVariable("id") long id) {
    return GreetingModel.Greeting.newBuilder()
        .setId(id)
        .setText("Hi, " + id + ", now it is " + System.currentTimeMillis() + " ms")
        .build();
  }
}
