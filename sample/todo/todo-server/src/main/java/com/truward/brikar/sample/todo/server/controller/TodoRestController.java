package com.truward.brikar.sample.todo.server.controller;

import com.truward.brikar.sample.todo.model.TodoModel;
import com.truward.brikar.sample.todo.model.TodoRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/todo")
public final class TodoRestController implements TodoRestService {

  @Override
  public TodoModel.ItemList getAllItems() {
    return TodoModel.ItemList.newBuilder()
        .addItems(TodoModel.Item.newBuilder()
            .setId(10L).setName("Run Service!").build())
        .build();
  }
}
