package com.viettel.deliverymanagement.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TrackingRateLimitFilterTest {

    @Test
    void blocksAnonymousTrackingAfterThirtyRequestsPerMinute() throws Exception {
        TrackingRateLimitFilter filter = new TrackingRateLimitFilter();
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracking/VT12345678");
        request.setServletPath("/tracking/VT12345678");
        request.setRemoteAddr("203.0.113.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int attempt = 0; attempt < 31; attempt++) {
            filter.doFilter(request, response, chain);
        }

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("RATE_LIMITED"));
        verify(chain, times(30)).doFilter(request, response);
    }
}
