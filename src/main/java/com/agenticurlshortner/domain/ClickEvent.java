package com.agenticurlshortner.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortKey;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime clickTime;

    public ClickEvent() {}

    public ClickEvent(String shortKey, String ipAddress, String userAgent) {
        this.shortKey = shortKey;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.clickTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getShortKey() { return shortKey; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getClickTime() { return clickTime; }
}