package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    
    /**
     * Tìm tất cả UserChallenge của một user, sắp xếp theo submitTime DESC (mới nhất trước)
     */
    @Query("SELECT uc FROM UserChallenge uc WHERE uc.user.id = :userId ORDER BY uc.submitTime DESC")
    List<UserChallenge> findAllByUserIdOrderBySubmitTimeDesc(@Param("userId") Long userId);
}
