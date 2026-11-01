package com.agenticurlshortner.repository;

import com.agenticurlshortner.domain.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    long countByShortKey(String shortKey);
}