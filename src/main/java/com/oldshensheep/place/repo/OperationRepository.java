package com.oldshensheep.place.repo;

import com.oldshensheep.place.entity.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findByCreatedAtBetween(Instant createdAtStart, Instant createdAtEnd, Pageable pageable);
    @Query("select o from Operation o where o.user.ip = ?1 order by o.createdAt asc nulls first ")
    Optional<Operation> findByUser_IpOrderByCreatedAtAsc(String ip);
}