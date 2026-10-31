package com.agenticurlshortner.repository;

import com.agenticurlshortner.orchestration.ExecutionContext;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionContextRepository extends JpaRepository<ExecutionContext, Long> {
}