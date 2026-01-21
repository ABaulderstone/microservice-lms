
package com.example.auth_service.user.kafka;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.events.user.v1.UserCreatedEvent;

import jakarta.annotation.PostConstruct;

@Component
public class UserEventPublisher {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    public UserEventPublisher(
            KafkaTemplate<Long, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserCreatedEvent(Long userId) {
        UserCreatedEvent.UserCreated event = UserCreatedEvent.UserCreated.newBuilder()
                .setUserId(userId)
                .setOccurredAt(System.currentTimeMillis())
                .build();
        System.out.println("Publishing UserCreatedEvent for userId: " + userId);
        System.out.println(kafkaTemplate);

        kafkaTemplate.send(
                "user.created.v1",
                userId,
                event.toByteArray());
    }

    @PostConstruct
    public void debugKafkaProducer() {
        if (kafkaTemplate.getProducerFactory() instanceof DefaultKafkaProducerFactory<?, ?> pf) {
            System.out.println("Kafka producer config:");
            pf.getConfigurationProperties()
                    .forEach((k, v) -> System.out.println(k + " = " + v));
        }
    }

}
