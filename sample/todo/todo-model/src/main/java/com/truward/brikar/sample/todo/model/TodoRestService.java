package com.truward.brikar.sample.todo.model;

import org.springframework.web.bind.annotation.*;

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
