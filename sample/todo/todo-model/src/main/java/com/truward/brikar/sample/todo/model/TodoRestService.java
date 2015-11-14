package com.truward.brikar.sample.todo.model;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
public interface TodoRestService {

  @RequestMapping("/items")
  @ResponseBody
  TodoModel.ItemList getAllItems();

  @RequestMapping("/items/{pos}")
  @ResponseBody
  TodoModel.Item getItem(@PathVariable("pos") int pos);
}
