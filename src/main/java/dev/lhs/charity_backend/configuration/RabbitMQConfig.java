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

/**
 * Cấu hình RabbitMQ
 * - Exchange và Queue cho bid processing
 * - Exchange và Queue cho finalization (delayed)
 */
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

    // Finalization Exchange và Queue (Delayed)
    // Note: Delayed exchange cần RabbitMQ Delayed Message Plugin
    // Nếu không có plugin, có thể dùng TTL + Dead Letter Exchange
    @Bean
    public TopicExchange finalizationExchange() {
        // Sử dụng CustomExchange với type "x-delayed-message" nếu có plugin
        // Hoặc dùng TopicExchange thông thường và handle delay ở application level
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
        factory.setConcurrentConsumers(3); // 3 workers xử lý bid
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}

