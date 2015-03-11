package com.truward.brikar.sample.zoo.model;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
public interface ZooRestService {

  @RequestMapping("/animal/{id}")
  @ResponseBody
  ZooModel.Animal getAnimal(@PathVariable("id") long id);
}
