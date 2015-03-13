package com.truward.brikar.sample.zoo.client;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.zoo.model.ZooModel;
import com.truward.brikar.sample.zoo.model.ZooRestService;

import java.net.URI;

/**
 * @author Alexander Shabanov
 */
public final class ZooClient {

  public static void main(String[] args) {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final ZooRestService service = restBinder.newClient(ZooRestService.class)
          .setUsername("testonly").setPassword("test")
          .setUri(URI.create("http://127.0.0.1:8080/rest/zoo"))
          .build();

      final ZooModel.Animal animal = service.getAnimal(1L);
      System.out.println("animal = " + animal);

      final ZooModel.Animal unkAnimal = service.getAnimal(2L);
      System.out.println("unknown animal = " + unkAnimal);
    }
  }
}
