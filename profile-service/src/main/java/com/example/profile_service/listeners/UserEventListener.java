package com.example.profile_service.listeners;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.profile_service.profile.ProfileService;
import com.google.protobuf.InvalidProtocolBufferException;

import jakarta.annotation.PostConstruct;

import com.example.events.user.v1.UserCreatedEvent;

@Component
public class UserEventListener {
    private final ProfileService profileService;
    @Autowired(required = false)
    private ConsumerFactory<?, ?> consumerFactory;

    public UserEventListener(ProfileService profileService) {
        this.profileService = profileService;
        System.out.println("UserEventListener initialized");
    }

    @PostConstruct
    public void debugConsumerConfig() {
        System.out.println("Consumer is active and listening to: user.created.v1");
    }

    @PostConstruct
    public void debugConfig() {
        if (consumerFactory instanceof DefaultKafkaConsumerFactory<?, ?> cf) {
            System.out.println("=== CONSUMER CONFIG ===");
            cf.getConfigurationProperties()
                    .forEach((k, v) -> System.out.println(k + " = " + v));
            System.out.println("======================");
        }
    }

    @KafkaListener(topics = "user.created.v1", groupId = "profile-service")
    public void onUserCreated(ConsumerRecord<Long, byte[]> record) {
        Long userId = record.key();
        byte[] data = record.value();

        System.out.println("Received event for userId=" + userId);

        try {
            UserCreatedEvent.UserCreated event = UserCreatedEvent.UserCreated.parseFrom(data);
            System.out.println("Processing UserCreatedEvent for userId: " + event.getUserId());
            profileService.createProfileIfNotExists(userId);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
