package com.truward.brikar.sample.zoo.server.controller;

import com.truward.brikar.sample.zoo.model.ZooModel;
import com.truward.brikar.sample.zoo.model.ZooRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/api/zoo")
public final class ZooRestController implements ZooRestService {

  @Override
  public ZooModel.Animal getAnimal(@PathVariable("id") long id) {
    if (id == 1L) {
      return ZooModel.Animal.newBuilder()
          .setId(id)
          .setName("crow")
          .build();
    }
    return null;
  }
}
