package com.agenticurlshortner.service;

import com.agenticurlshortner.domain.ShortUrl;
import com.agenticurlshortner.domain.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UrlService {
    private final ShortUrlRepository repository;

    public UrlService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String shortenUrl(String originalUrl) {

        String key = UUID.randomUUID().toString().substring(0, 8);

        ShortUrl url = new ShortUrl();
        url.setOriginalUrl(originalUrl);
        url.setShortKey(key);
        repository.save(url);

        return key;
    }

    public Optional<String> getOriginalUrl(String shortKey) {
        return repository.findByShortKey(shortKey)
                .map(ShortUrl::getOriginalUrl);
    }
}