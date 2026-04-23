package com.rakhtsaathi.repository;

import com.rakhtsaathi.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByBloodRequestId(Long bloodRequestId);
    List<Feedback> findByToUserId(Long toUserId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.toUser.id = :userId")
    Double findAverageRatingByToUserId(@Param("userId") Long userId);
}
