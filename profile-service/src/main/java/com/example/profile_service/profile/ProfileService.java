package com.example.profile_service.profile;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import com.example.profile_service.profile.entities.Profile;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public void createProfileIfNotExists(Long userId) {
        if (profileRepository.existsByUserId(userId)) {
            return;
        }
        Profile profile = new Profile(userId);
        profileRepository.save(profile);
    }
}
