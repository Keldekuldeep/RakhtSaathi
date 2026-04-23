package com.rakhtsaathi.repository;

import com.rakhtsaathi.entity.Needy;
import com.rakhtsaathi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NeedyRepository extends JpaRepository<Needy, Long> {
    Optional<Needy> findByUser(User user);
    Optional<Needy> findByUserId(Long userId);
}
