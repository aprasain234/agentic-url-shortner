package com.agenticurlshortner.controller;

import com.agenticurlshortner.domain.ClickEvent;
import com.agenticurlshortner.repository.ClickEventRepository;
import com.agenticurlshortner.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/url")
public class UrlController {
    private final UrlService urlService;
    private final ClickEventRepository clickEventRepository;

    public UrlController(UrlService urlService, ClickEventRepository clickEventRepository) {
        this.urlService = urlService;
        this.clickEventRepository = clickEventRepository;
    }

    @PostMapping("/shorten")
    public ResponseEntity shorten(@RequestBody Map<String, String> payload) {
        String originalUrl = payload.get("originalUrl");
        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String shortKey = urlService.shortenUrl(originalUrl);
        return ResponseEntity.ok(Map.of("shortKey", shortKey));
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey, HttpServletRequest request) {
        return urlService.getOriginalUrl(shortKey)
                .map(url -> {
                    // Record analytics event before redirecting
                    clickEventRepository.save(new ClickEvent(
                            shortKey,
                            request.getRemoteAddr(),
                            request.getHeader("User-Agent")
                    ));

                    HttpHeaders headers = new HttpHeaders();
                    headers.setLocation(URI.create(url));
                    return new ResponseEntity<Void>(headers, HttpStatus.FOUND);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{shortKey}/stats")
    public ResponseEntity getUrlStats(@PathVariable String shortKey) {
        long totalClicks = clickEventRepository.countByShortKey(shortKey);
        return ResponseEntity.ok(Map.of(
                "shortKey", shortKey,
                "totalClicks", totalClicks
        ));
    }
}