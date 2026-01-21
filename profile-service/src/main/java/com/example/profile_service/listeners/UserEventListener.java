package com.example.profile_service.listeners;

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
    }

    @KafkaListener(topics = "user.created.v1", groupId = "profile-service")

    public void onUserCreated(@Header(KafkaHeaders.RECEIVED_KEY) Long userId, byte[] data) {
        try {
            UserCreatedEvent.UserCreated event = UserCreatedEvent.UserCreated.parseFrom(data);
            System.out.println("Received UserCreatedEvent for userId: " + event.getUserId());
            profileService.createProfileIfNotExists(userId);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to parse user created event", e);
        }
    }
}
