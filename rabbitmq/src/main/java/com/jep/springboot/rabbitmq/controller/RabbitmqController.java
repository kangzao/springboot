package com.jep.springboot.rabbitmq.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @author enping.jep
 * @create 2019-03-13 7:57 PM
 */
@RestController
public class RabbitmqController {

  private Logger logger = LoggerFactory.getLogger(RabbitmqController.class);

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @RequestMapping("/sendMsg")
  public Object sendMsg() {
    logger.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")));
    rabbitTemplate.convertAndSend("couponExchange", "coupon", "coupon");
    return "success";
  }

}
