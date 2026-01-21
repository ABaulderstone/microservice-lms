package com.example.profile_service.profile.grpc;

import io.grpc.Status;

import com.example.profile.proto.v1.ProfileRequest;
import com.example.profile.proto.v1.ProfileResponse;
import com.example.profile.proto.v1.ProfileServiceGrpc.ProfileServiceImplBase;
import com.example.profile_service.profile.ProfileService;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ProfileGrpcService extends ProfileServiceImplBase {
    private final ProfileService profileService;

    public ProfileGrpcService(ProfileService profileService) {
        this.profileService = profileService;
        System.out.println("ProfileGrpcService initialized");
    }

    public void findProfile(ProfileRequest profileRequest, StreamObserver<ProfileResponse> responseObserver) {
        long userId = profileRequest.getUserId();
        var foundProfile = profileService.findByUserId(userId).orElse(null);
        if (foundProfile == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Profile with user ID " + userId + " not found")
                    .asRuntimeException());
            return;
        }
        var response = ProfileResponse.newBuilder()
                .setId(foundProfile.getId())
                .setUserId(foundProfile.getUserId())
                .setFirstName(foundProfile.getFirstName())
                .setLastName(foundProfile.getLastName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
