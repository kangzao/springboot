package com.jep.springboot.rabbitmq.config;


import com.jep.springboot.rabbitmq.constants.RabbitConstants;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @author enping.jep
 * @create 2019-03-13 3:55 PM
 */
@Configuration
public class RabbitMQConfig {

  private Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);


  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    return rabbitTemplate;
  }

  /****fanout模式 将交换器和队列直接绑定**/
  @Bean
  public FanoutExchange fanoutExchange() {
    return new FanoutExchange(RabbitConstants.FANOUT_EXCHANGE);
  }

  @Bean
  public Queue fanoutQueue() {
    return new Queue(RabbitConstants.FANOUT_QUEUE);
  }

  @Bean
  public Binding bindingFanoutQueue() {
    return BindingBuilder.bind(fanoutQueue()).to(fanoutExchange());
  }

  @RabbitListener(queues = {RabbitConstants.FANOUT_QUEUE})
  public void listenFanoutQueue(Message message) {
    String msg = new String(message.getBody());
    logger.info("listenFanoutQueue receive ==" + msg);
  }

  /****fanout模式**/


  /******* topic模式 ****/
  @Bean
  public TopicExchange exchange001() {
    return new TopicExchange("topic001", true, false);
  }

  @Bean
  public Queue queue001() {
    return new Queue("queue001", true); //队列持久
  }

  @Bean
  public Binding binding001() {
    return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.*");
  }

  @Bean
  public TopicExchange exchange002() {
    return new TopicExchange("topic002", true, false);
  }


  @Bean
  public Queue queue002() {
    return new Queue("queue002", true); //队列持久
  }

  @Bean
  public Binding binding002() {
    return BindingBuilder.bind(queue002()).to(exchange002()).with("rabbit.*");
  }


  @Bean
  public TopicExchange exchange003() {
    return new TopicExchange("topic003", true, false);
  }

  @Bean
  public Queue queue003() {
    return new Queue("queue003", true); //队列持久
  }

  @Bean
  public Binding binding003() {
    return BindingBuilder.bind(queue003()).to(exchange003()).with("mq.*");
  }

  @RabbitListener(queues = {"queue001", "queue002"})
  public void listener(Message message) {
    String msg = new String(message.getBody());
    MessageProperties megProperties = message.getMessageProperties();
    //模拟业务操作
    logger.info("RabbitListener msg=============================" + msg);
    logger.info(
        "RabbitListener megProperties=============================" + megProperties.getHeaders()
            .toString());
  }

  @RabbitListener(queues = {"queue003"})
  public void listener_(Message message) {
    String msg = new String(message.getBody());
    logger.info("queue003===========RabbitListener =============================" + msg);
  }


  /*** 声明优惠券队列（Fanout类型的exchange）**/
  @Bean
  public Queue couponQueue() {
    // 当队列中的消息超时，按照新的exchange和routing-key路由
    // 设置消息的过期时间， 单位是毫秒
    return QueueBuilder.durable("couponQueue")
        .withArgument("x-dead-letter-exchange", "deadLetterExchange")
        .withArgument("x-dead-letter-routing-key", "deadLetterQueue")
        .withArgument("x-message-ttl", 5000).build();
//    Map<String, Object> args = new HashMap<>();
//
//    args.put("x-dead-letter-exchange", "deadLetterExchange");
//    args.put("x-dead-letter-routing-key", "deadLetterQueue");
//
//    args.put("x-message-ttl", 5000);
//
//    // 是否持久化
//    boolean durable = true;
//    // 仅创建者可以使用的私有队列，断开后自动删除
//    boolean exclusive = false;
//    // 当所有消费客户端连接断开后，是否自动删除队列
//    boolean autoDelete = false;
//    return new Queue("couponQueue", durable, exclusive, autoDelete, args);
  }


  /*** 声明优惠券交换机 */
  @Bean
  public TopicExchange couponExchange() {
    return new TopicExchange("couponExchange", true, false);
  }

  /**
   * 绑定优惠券队列
   */
  @Bean
  public Binding bindingCouponQueue() {
    return BindingBuilder.bind(couponQueue()).to(couponExchange()).with("coupon");
  }


  /***定义死信队列*/
  @Bean
  public Queue deadLetterQueue() {
    return new Queue("deadLetterQueue", true); //队列持久
  }


  /******* 死信队列交换机 ****/
  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange("deadLetterExchange", true, false);
  }

  @Bean
  public Binding bindingDeadLetter() {
    return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("deadLetterQueue");
  }


  @RabbitListener(queues = {"deadLetterQueue"})
  public void listenDeadQueue(Message message) {
    logger.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")));
    String msg = new String(message.getBody());
    logger.info("===========deadQueue =============================" + msg);
  }

}
