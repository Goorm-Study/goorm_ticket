package com.example.goorm_ticket.lockdomain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LockRepository extends JpaRepository<LockEntity, Long> {
    @Query(value = "SELECT GET_LOCK(:lockKey, :waitTime)", nativeQuery = true)
    void getLock(@Param("lockKey") String lockKey, @Param("waitTime") int waitTime);

    @Query(value = "SELECT RELEASE_LOCK(:lockKey)", nativeQuery = true)
    void releaseLock(@Param("lockKey") String lockKey);
}
