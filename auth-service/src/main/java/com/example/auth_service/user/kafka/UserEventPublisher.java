
package com.example.auth_service.user.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.events.user.v1.UserCreatedEvent;

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

        kafkaTemplate.send(
                "user.created.v1",
                userId,
                event.toByteArray());
    }
}
