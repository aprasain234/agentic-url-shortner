package com.agenticurlshortner;

import com.agenticurlshortner.repository.ClickEventRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {"OPENAI_API_KEY=dummy-test-key"})
class UrlShortenerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ClickEventRepository clickEventRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldShortenUrlAndRedirectAndRecordAnalytics() throws Exception {
        String originalUrl = "https://www.schwab.com/engineering";

        // 1. Shorten the URL via the baseline REST API
        MvcResult result = mockMvc.perform(post("/api/url/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\": \"" + originalUrl + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String shortKey = JsonPath.read(responseBody, "$.shortKey");

        long initialEventCount = clickEventRepository.count();

        // 2. Access the short URL to trigger the redirect mechanism
        mockMvc.perform(get("/api/url/" + shortKey))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(originalUrl));

        // 3. Verify analytics were physically captured in the H2 database
        assertThat(clickEventRepository.count()).isEqualTo(initialEventCount + 1);
    }
}