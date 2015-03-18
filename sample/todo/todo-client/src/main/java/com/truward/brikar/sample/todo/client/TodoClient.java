package com.truward.brikar.sample.todo.client;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.todo.model.TodoModel;
import com.truward.brikar.sample.todo.model.TodoRestService;

import java.net.URI;

/**
 * @author Alexander Shabanov
 */
public final class TodoClient {

  public static void main(String[] args) {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final TodoRestService service = restBinder.newClient(TodoRestService.class)
          .setUsername("testonly").setPassword("test")
          .setUri(URI.create("http://127.0.0.1:8080/rest/todo"))
          .build();

      final TodoModel.ItemList items = service.getAllItems();
      System.out.println("items = " + items);
    }
  }
}
