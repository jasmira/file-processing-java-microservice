package com.gift.and.go.file_processor.repositories;

import com.gift.and.go.file_processor.entities.RequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLogEntity, Long> {
}
