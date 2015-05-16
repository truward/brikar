package com.truward.brikar.sample.todo.server.controller;

import com.truward.brikar.sample.todo.model.TodoModel;
import com.truward.brikar.sample.todo.model.TodoRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/todo")
public final class TodoRestController implements TodoRestService {
  private final TodoModel.Item item = TodoModel.Item.newBuilder().setId(10L).setName("Run Service!").build();

  @Override
  public TodoModel.ItemList getAllItems() {
    return TodoModel.ItemList.newBuilder().addItems(item).build();
  }

  @Override
  public TodoModel.Item getItem(@PathVariable("pos") int pos) {
    if (pos != 0) {
      throw new IllegalArgumentException("position is out of range: " + pos);
    }
    return item;
  }
}
