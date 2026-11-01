package com.agenticurlshortner;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"OPENAI_API_KEY=dummy-test-key"})
class AgenticUrlShortnerApplicationTests {

    @Test
    void contextLoads() {
    }
}