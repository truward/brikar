package com.truward.brikar.sample.todo.client;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.todo.model.TodoModel;
import com.truward.brikar.sample.todo.model.TodoRestService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Alexander Shabanov
 */
public final class TodoClient {

  public static void main(String[] args) throws IOException {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final TodoRestService service = restBinder.newClient(TodoRestService.class)
          .setUsername("todoServiceUser").setPassword("todoPassword")
          .setUri(URI.create("http://127.0.0.1:8080/rest/todo"))
          .build();

      if (args.length > 0 && "--repl".equals(args[0])) {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(System.in))) {
          startRepl(r, service);
        }
      } else {
        printAllItems(service);
      }
    }
  }

  private static void printHelp() {
    System.out.println("Commands: help, printTime, quit, getAllItems");
  }

  private static void startRepl(BufferedReader r, TodoRestService service) throws IOException {
    final SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");
    for (;;) {
      System.out.print("[" + format.format(new Date()) + "] > ");
      final String line = r.readLine();
      System.out.println();
      switch (line) {
        case "help":
          printHelp();
          break;

        case "printTime":
          System.out.println("Time=" + new Date());
          break;

        case "getAllItems":
          printAllItems(service);
          break;

        case "quit":case "exit":
          return;

        default:
          System.out.println("Unknown command: " + line);
          printHelp();
      }
    }
  }

  private static void printAllItems(TodoRestService service) {
    try {
      final TodoModel.ItemList items = service.getAllItems();
      System.out.println("service.getAllItems() = " + items);
    } catch (Exception e) {
      System.err.println("Service call failed");
      e.printStackTrace(System.err);
    }
  }
}
