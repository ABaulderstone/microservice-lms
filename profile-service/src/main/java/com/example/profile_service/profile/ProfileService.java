package com.example.profile_service.profile;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import com.example.profile_service.profile.entities.Profile;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        System.out.println("ProfileService initialized");
    }

    @Transactional
    public void createProfileIfNotExists(Long userId) {
        if (profileRepository.existsByUserId(userId)) {
            return;
        }
        Profile profile = new Profile(userId);
        profileRepository.save(profile);
    }

    public Optional<Profile> findByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }
}
