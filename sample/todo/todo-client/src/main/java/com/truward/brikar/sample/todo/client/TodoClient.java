package com.truward.brikar.sample.todo.client;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.todo.model.TodoModel;
import com.truward.brikar.sample.todo.model.TodoQueryService;
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

      final TodoQueryService queryService = restBinder.newClient(TodoQueryService.class)
          .setUsername("todoServiceUser").setPassword("todoPassword")
          .setUri(URI.create("http://127.0.0.1:8080/rest/todo"))
          .build();

      if (args.length > 0 && "--repl".equals(args[0])) {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(System.in))) {
          startRepl(r, service, queryService);
        }
      } else {
        printAllItems(service);
      }
    }
  }

  private static void printHelp() {
    System.out.println("Commands: help, printTime, quit, getAllItems, getItem0, getItem1, queryItems");
  }

  private static void startRepl(BufferedReader r,
                                TodoRestService service,
                                TodoQueryService queryService) throws IOException {
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

        case "getItem0":
          printItem(service, 0);
          break;

        case "getItem1":
          printItem(service, 1);
          break;

        case "queryItems":
          queryItems(queryService);
          break;

        case "quit":case "exit":
          return;

        default:
          System.out.println("Unknown command: " + line);
          printHelp();
      }
    }
  }

  private static void queryItems(TodoQueryService queryService) {
    try {
      final TodoModel.ItemList items = queryService.queryItems();
      System.out.println("queryService.queryItems() = " + items);
    } catch (Exception e) {
      System.err.println("Service call failed");
      e.printStackTrace(System.err);
    }
  }

  private static void printItem(TodoRestService service, int pos) {
    try {
      final TodoModel.Item item = service.getItem(pos);
      System.out.println("service.getItem(" + pos + ") = " + item);
    } catch (Exception e) {
      System.err.println("Service call failed");
      e.printStackTrace(System.err);
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
