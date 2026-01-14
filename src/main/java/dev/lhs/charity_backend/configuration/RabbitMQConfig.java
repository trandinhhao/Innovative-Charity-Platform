package dev.lhs.charity_backend.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.bid}")
    private String bidExchange;

    @Value("${rabbitmq.queue.bid}")
    private String bidQueue;

    @Value("${rabbitmq.routing-key.bid}")
    private String bidRoutingKey;

    @Value("${rabbitmq.exchange.finalization}")
    private String finalizationExchange;

    @Value("${rabbitmq.queue.finalization}")
    private String finalizationQueue;

    @Value("${rabbitmq.routing-key.finalization}")
    private String finalizationRoutingKey;

    // Bid Exchange và Queue
    @Bean
    public TopicExchange bidExchange() {
        return new TopicExchange(bidExchange);
    }

    @Bean
    public Queue bidQueue() {
        return QueueBuilder.durable(bidQueue).build();
    }

    @Bean
    public Binding bidBinding() {
        return BindingBuilder
                .bind(bidQueue())
                .to(bidExchange())
                .with(bidRoutingKey);
    }


    @Bean
    public TopicExchange finalizationExchange() {
        return ExchangeBuilder
                .topicExchange(finalizationExchange)
                .build();
    }

    @Bean
    public Queue finalizationQueue() {
        return QueueBuilder.durable(finalizationQueue).build();
    }

    @Bean
    public Binding finalizationBinding() {
        return BindingBuilder
                .bind(finalizationQueue())
                .to(finalizationExchange())
                .with(finalizationRoutingKey);
    }

    // Message Converter
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3); // 3 worker
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}

