package com.rakhtsaathi.repository;

import com.rakhtsaathi.entity.BloodRequest;
import com.rakhtsaathi.entity.Needy;
import com.rakhtsaathi.entity.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    Page<BloodRequest> findByNeedyOrderByCreatedAtDesc(Needy needy, Pageable pageable);

    Page<BloodRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    Page<BloodRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<BloodRequest> findByStatus(RequestStatus status);

    long countByStatus(RequestStatus status);

    @Query("SELECT br FROM BloodRequest br WHERE br.needy.id = :needyId ORDER BY br.createdAt DESC")
    Page<BloodRequest> findByNeedyId(@Param("needyId") Long needyId, Pageable pageable);
}
