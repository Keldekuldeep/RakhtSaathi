package com.rakhtsaathi.repository;

import com.rakhtsaathi.entity.Donor;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.entity.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByUser(User user);
    Optional<Donor> findByUserId(Long userId);

    List<Donor> findByCityAndBloodGroupAndIsAvailableTrue(String city, BloodGroup bloodGroup);

    @Query("SELECT d FROM Donor d WHERE d.city = :city AND d.isAvailable = true")
    List<Donor> findAvailableDonorsByCity(@Param("city") String city);

    @Query("SELECT d FROM Donor d WHERE d.bloodGroup IN :bloodGroups AND d.city = :city AND d.isAvailable = true")
    List<Donor> findCompatibleDonors(@Param("bloodGroups") List<BloodGroup> bloodGroups,
                                     @Param("city") String city);

    long countByIsAvailableTrue();
}
