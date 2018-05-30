package io.fundrequest.azrael.worker.config;

import io.fundrequest.azrael.worker.contracts.claim.sign.ClaimService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("azrael-exchange");
    }

    @Bean
    SimpleMessageListenerContainer claimContainer(ConnectionFactory connectionFactory,
                                                  MessageListenerAdapter approvedClaimListenerAdapter,
                                                  @Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setDefaultRequeueRejected(false);
        container.setMessageListener(approvedClaimListenerAdapter);
        return container;
    }


    @Bean
    MessageListenerAdapter approvedClaimListenerAdapter(ClaimService receiver) {
        return new MessageListenerAdapter(receiver, "receiveApprovedClaim");
    }

    @Bean
    Queue approvedClaimQueue(@Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        return declareQueue(queueName);
    }

    @Bean
    Queue approvedClaimQueueDLQ(@Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        return declareDLQ(queueName);
    }

    @Bean
    Binding approvedClaimBinding(Queue approvedClaimQueue, TopicExchange exchange, @Value("${io.fundrequest.azrael.queue.approved-claim}") final String queueName) {
        return BindingBuilder.bind(approvedClaimQueue).to(exchange).with(queueName);
    }

    private Queue declareQueue(@Value("${io.fundrequest.azrael.queue.claim}") String queueName) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "");
        arguments.put("x-dead-letter-routing-key", queueName + ".dlq");
        return new Queue(queueName, true, false, false, arguments);
    }

    private Queue declareDLQ(@Value("${io.fundrequest.azrael.queue.fund}") String queueName) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-mode", "lazy");
        Queue queue = new Queue(queueName + ".dlq", true, false, false, arguments);
        queue.setAdminsThatShouldDeclare(rabbitAdmin());
        rabbitAdmin().declareQueue(queue);
        return queue;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory);
    }
}
