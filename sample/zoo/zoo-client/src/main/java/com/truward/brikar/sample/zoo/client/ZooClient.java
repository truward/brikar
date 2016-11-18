package com.truward.brikar.sample.zoo.client;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.client.rest.ServiceClientCredentials;
import com.truward.brikar.client.rest.support.StandardRestServiceBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.zoo.model.ZooModel;
import com.truward.brikar.sample.zoo.model.ZooRestService;

import java.net.URI;
import java.util.Collections;

/**
 * @author Alexander Shabanov
 */
public final class ZooClient {

  public static void main(String[] args) {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufHttpMessageConverter())) {
      final URI uri = URI.create("http://127.0.0.1:8080/api/zoo");
      rof.setCredentials(Collections.singletonList(new ServiceClientCredentials(uri,
          "testonly", "test")));

      final ZooRestService service = new StandardRestServiceBinder(rof.getRestOperations())
          .createClient(uri, ZooRestService.class);

      final ZooModel.Animal animal = service.getAnimal(1L);
      System.out.println("animal = " + animal);

      final ZooModel.Animal unkAnimal = service.getAnimal(2L);
      System.out.println("unknown animal = " + unkAnimal);
    }
  }
}
