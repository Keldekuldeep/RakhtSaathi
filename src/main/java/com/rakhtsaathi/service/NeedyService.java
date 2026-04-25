package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.request.NeedyProfileRequest;
import com.rakhtsaathi.dto.response.NeedyProfileResponse;
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
    public NeedyProfileResponse createProfile(User user, NeedyProfileRequest request) {
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
                .phone(request.getPhone())
                .address(request.getAddress())
                .state(request.getState())
                .pincode(request.getPincode())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactRelation(request.getEmergencyContactRelation())
                .requestCount(0)
                .build();

        Needy saved = needyRepository.save(needy);
        log.info("Needy profile created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public NeedyProfileResponse updateProfile(User user, NeedyProfileRequest request) {
        Needy needy = needyRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Needy profile not found"));

        needy.setCity(request.getCity());
        needy.setAge(request.getAge());
        needy.setGender(request.getGender());
        needy.setRelationToPatient(request.getRelationToPatient());

        // Update optional fields if provided
        if (request.getPhone() != null) needy.setPhone(request.getPhone());
        if (request.getAddress() != null) needy.setAddress(request.getAddress());
        if (request.getState() != null) needy.setState(request.getState());
        if (request.getPincode() != null) needy.setPincode(request.getPincode());
        if (request.getEmergencyContactName() != null) needy.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) needy.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getEmergencyContactRelation() != null) needy.setEmergencyContactRelation(request.getEmergencyContactRelation());

        return toResponse(needyRepository.save(needy));
    }

    public NeedyProfileResponse getProfileResponse(User user) {
        Needy needy = needyRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Needy profile not found. Please complete registration."));
        return toResponse(needy);
    }

    public Needy getProfile(User user) {
        return needyRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Needy profile not found. Please complete registration."));
    }

    public Needy getById(Long id) {
        return needyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Needy", id));
    }

    public NeedyProfileResponse toResponse(Needy needy) {
        return NeedyProfileResponse.builder()
                .id(needy.getId())
                .userId(needy.getUser().getId())
                .fullName(needy.getUser().getFullName())
                .email(needy.getUser().getEmail())
                .city(needy.getCity())
                .age(needy.getAge())
                .gender(needy.getGender())
                .relationToPatient(needy.getRelationToPatient())
                .phone(needy.getPhone())
                .address(needy.getAddress())
                .state(needy.getState())
                .pincode(needy.getPincode())
                .emergencyContactName(needy.getEmergencyContactName())
                .emergencyContactPhone(needy.getEmergencyContactPhone())
                .emergencyContactRelation(needy.getEmergencyContactRelation())
                .requestCount(needy.getRequestCount())
                .createdAt(needy.getCreatedAt())
                .updatedAt(needy.getUpdatedAt())
                .build();
    }
}
