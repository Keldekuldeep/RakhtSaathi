package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.request.NeedyProfileRequest;
import com.rakhtsaathi.entity.Needy;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.exception.ResourceNotFoundException;
import com.rakhtsaathi.repository.NeedyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeedyService {

    private final NeedyRepository needyRepository;

    @Transactional
    public Needy createProfile(User user, NeedyProfileRequest request) {
        log.info("Creating needy profile for user: {}", user.getEmail());

        if (needyRepository.findByUser(user).isPresent()) {
            throw new IllegalArgumentException("Needy profile already exists for this user");
        }

        Needy needy = Needy.builder()
                .user(user)
                .city(request.getCity())
                .age(request.getAge())
                .gender(request.getGender())
                .relationToPatient(request.getRelationToPatient())
                .requestCount(0)
                .build();

        Needy saved = needyRepository.save(needy);
        log.info("Needy profile created with id: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Needy updateProfile(User user, NeedyProfileRequest request) {
        Needy needy = needyRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Needy profile not found"));

        needy.setCity(request.getCity());
        needy.setAge(request.getAge());
        needy.setGender(request.getGender());
        needy.setRelationToPatient(request.getRelationToPatient());

        return needyRepository.save(needy);
    }

    public Needy getProfile(User user) {
        return needyRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Needy profile not found. Please complete registration."));
    }

    public Needy getById(Long id) {
        return needyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Needy", id));
    }
}
