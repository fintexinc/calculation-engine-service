package com.fintex.ce.adapter.rest.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TasksController {

  @PostMapping(value = "garbage-collection")
  public void garbageCollection() {
    System.gc();
  }

}
