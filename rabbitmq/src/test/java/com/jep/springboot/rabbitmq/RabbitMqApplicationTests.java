package com.jep.springboot.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jep.springboot.rabbitmq.constants.RabbitConstants;
import com.jep.springboot.rabbitmq.entity.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

/*
 * @author enping.jep
 * @create 2019-03-13 7:02 PM
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMqApplicationTests {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  private Logger logger = LoggerFactory.getLogger(RabbitMqApplicationTests.class);

  @Test
  public void contextLoads() {
  }

  @Test
  public void testSendFanoutMsg() throws JsonProcessingException {
    //将对象转化成string
    Order order = new Order();
    order.setId("001");
    order.setName("消息订单");
    order.setContent("描述信息");
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(order);

    //构建消息体
    MessageProperties messageProperties = new MessageProperties();
    //这里注意一定要修改contentType为 application/json
    messageProperties.setContentType("application/json");
    Message message = new Message(json.getBytes(), messageProperties);
    StopWatch watch = new StopWatch();
    watch.start();
    rabbitTemplate.send(RabbitConstants.FANOUT_EXCHANGE, "fanout mode ingore routingkey", message);
    watch.stop();
    logger.info("发送消息耗时：" + watch.getTotalTimeSeconds() + "s");

  }


  @Test
  public void testSendMessage() throws Exception {
    //1 创建消息
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.getHeaders().put("desc", "信息描述..");
    messageProperties.getHeaders().put("type", "自定义消息类型..");
    Message message = new Message("Hello RabbitMQ".getBytes(), messageProperties);

    rabbitTemplate.convertAndSend("topic001", "spring.amqp", message, new MessagePostProcessor() {
      @Override
      public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().getHeaders().put("desc", "额外修改的信息描述");
        message.getMessageProperties().getHeaders().put("attr", "额外新加的属性");
        return message;
      }
    });
  }

  @Test
  public void testSendMessage2() throws Exception {
    //1 创建消息
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setContentType("text/plain");
    Message message = new Message("mq 消息1234".getBytes(), messageProperties);
    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

    rabbitTemplate.send("topic001", "spring.abc", message);

    rabbitTemplate.convertAndSend("topic001", "spring.amqp", "hello object message send!");
    rabbitTemplate.convertAndSend("topic002", "rabbit.abc", "hello object message send!");
  }


  @Test
  public void testSendMessage3() throws Exception {
    //1 创建消息
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setContentType("text/plain");

    rabbitTemplate.convertAndSend("topic003", "mq.abc", "mq.abc");
  }

  @Test
  public void testSendJsonMessage() throws Exception {
    Order order = new Order();
    order.setId("001");
    order.setName("消息订单");
    order.setContent("描述信息");
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(order);
    System.err.println("order 4 json: " + json);

    MessageProperties messageProperties = new MessageProperties();
    //这里注意一定要修改contentType为 application/json
    messageProperties.setContentType("application/json");
    Message message = new Message(json.getBytes(), messageProperties);
    rabbitTemplate.send("topic001", "spring.order", message);
  }



}
