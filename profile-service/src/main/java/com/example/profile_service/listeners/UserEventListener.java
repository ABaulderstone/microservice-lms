package com.example.profile_service.listeners;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.profile_service.profile.ProfileService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.example.events.user.v1.UserCreatedEvent;

@Component
public class UserEventListener {
    private final ProfileService profileService;

    public UserEventListener(ProfileService profileService) {
        this.profileService = profileService;
        System.out.println("UserEventListener initialized");
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
