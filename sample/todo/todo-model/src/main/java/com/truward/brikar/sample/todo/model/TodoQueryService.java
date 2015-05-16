package com.truward.brikar.sample.todo.model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
public interface TodoQueryService {

  @RequestMapping(value = "/items/query", method = RequestMethod.POST)
  @ResponseBody
  TodoModel.ItemList queryItems();
}
