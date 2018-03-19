package io.fundrequest.azrael.worker.config;

import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmsConfig {

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("azrael-exchange");
    }

    @Bean
    SimpleMessageListenerContainer claimContainer(ConnectionFactory connectionFactory,
                                                  MessageListenerAdapter approvedClaimListenerAdapter,
                                                  @Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(approvedClaimListenerAdapter);
        return container;
    }


    @Bean
    MessageListenerAdapter approvedClaimListenerAdapter(ClaimService receiver) {
        return new MessageListenerAdapter(receiver, "receiveApprovedClaim");
    }

    @Bean
    Queue approvedClaimQueue(@Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        return new Queue(queueName, true);
    }


    @Bean
    Binding approvedClaimBinding(Queue claimQueue, TopicExchange exchange, @Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        return BindingBuilder.bind(claimQueue).to(exchange).with(queueName);
    }
}
