package com.siryus.swisscon.api.config;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CustomCorsFilterTest {

    private static final String TEST_ORIGIN = "https://www.example.com/randomPath";

    @InjectMocks
    private CustomCorsFilter corsFilter = new CustomCorsFilter();

    @Test
    public void testFilterWithWildCard() throws Exception {

        // test only with wildcard
        initWithAllowedOriginEnvVars("*");
        MockHttpServletResponse response = filterWithMockClasses(TEST_ORIGIN);
        assertAccessControlHeaders(response, TEST_ORIGIN);

        // test with wildcard and invalid url
        initWithAllowedOriginEnvVars("*,abc");
        MockHttpServletResponse response2 = filterWithMockClasses(TEST_ORIGIN);
        assertAccessControlHeaders(response2, TEST_ORIGIN);

    }

    @Test
    public void testFilterWithLocalHost() throws Exception {

        // test correct localhost origin
        initWithAllowedOriginEnvVars("*.siryus.com");
        MockHttpServletResponse response = filterWithMockClasses("https://localhost:4200");
        assertAccessControlHeaders(response, "https://localhost:4200");

        // test fake localhost origin
        MockHttpServletResponse response2 = filterWithMockClasses("https://fake.localhost.com");
        assertNullAccessControlHeaders(response2);
    }

    @Test
    public void testFilterSubdomainWildCard() throws Exception {

        // test normal domain
        initWithAllowedOriginEnvVars("*.siryus.com,*.random.org,*.example.co.uk,test.test.com");
        MockHttpServletResponse response = filterWithMockClasses("https://www.siryus.com");
        assertAccessControlHeaders(response, "https://www.siryus.com");

        // test normal domain with path
        MockHttpServletResponse response2 = filterWithMockClasses("https://www.siryus.com/random/path/to/451");
        assertAccessControlHeaders(response2, "https://www.siryus.com/random/path/to/451");

        // test subdomain without path
        MockHttpServletResponse response3 = filterWithMockClasses("https://other-sub-domain.siryus.com");
        assertAccessControlHeaders(response3, "https://other-sub-domain.siryus.com");

        // test without subdomain without path
        MockHttpServletResponse response4 = filterWithMockClasses("https://siryus.com");
        assertAccessControlHeaders(response4, "https://siryus.com");

        // test without subdomain with path
        MockHttpServletResponse response5 = filterWithMockClasses("https://siryus.com/path/test");
        assertAccessControlHeaders(response5, "https://siryus.com/path/test");

        // test with co.uk
        MockHttpServletResponse response6 = filterWithMockClasses("https://test.example.co.uk/path");
        assertAccessControlHeaders(response6, "https://test.example.co.uk/path");

        // test with wrong url
        MockHttpServletResponse response7 = filterWithMockClasses("https://test.notmatching.com");
        assertNullAccessControlHeaders(response7);

        // test with wrong url
        MockHttpServletResponse response8 = filterWithMockClasses("https://notmatching.com");
        assertNullAccessControlHeaders(response8);

        // test with invalid url
        MockHttpServletResponse response9 = filterWithMockClasses("siryus.com.blabla.xxx");
        assertNullAccessControlHeaders(response9);
    }

    @Test
    public void testPreflightRequest() throws IOException, ServletException {
        initWithAllowedOriginEnvVars("*.siryus.com");
        MockHttpServletResponse response = filterWithMockClasses("https://dev-project.siryus.com", true);
        assertAccessControlHeaders(response, "https://dev-project.siryus.com");
        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatus());
    }

    @Test
    public void testFilterExactDomain() throws Exception {

        // test normal domain
        initWithAllowedOriginEnvVars("dev-project.siryus.com,*.example.co.uk");
        MockHttpServletResponse response = filterWithMockClasses("https://dev-project.siryus.com");
        assertAccessControlHeaders(response, "https://dev-project.siryus.com");

        // test normal domain with wrong sub-domain
        MockHttpServletResponse response2 = filterWithMockClasses("https://prod-project.siryus.com");
        assertNullAccessControlHeaders(response2);
    }

    @Test
    public void testFailedInit() {
        // if no allowed origins are set > we cannot startup the cors filter
        assertThrows(RuntimeException.class, () -> {
            initWithAllowedOriginEnvVars(null);
            filterWithMockClasses("this will not work");
        });
    }

    private void initWithAllowedOriginEnvVars(String allowedOrigins) {
        ReflectionTestUtils.setField(corsFilter, "allowedOriginsEnv", allowedOrigins);
        corsFilter.init(null);
    }

    private void assertAccessControlHeaders(MockHttpServletResponse response, String expectedOrigin) {
        assertEquals(expectedOrigin, response.getHeader(CustomCorsFilter.ALLOW_ORIGIN_HEADER), "Response Header should contain Origin Header");
        assertEquals(CustomCorsFilter.ALLOWED_HTTP_METHODS, response.getHeader(CustomCorsFilter.ALLOW_METHODS_HEADER), "Response Header should contain Methods Header");
        assertEquals(CustomCorsFilter.ALLOWED_HTTP_HEADERS, response.getHeader(CustomCorsFilter.ALLOW_HEADERS_HEADER), "Response Header should contain Headers Header");
        assertEquals("true", response.getHeader(CustomCorsFilter.ALLOW_CREDENTIALS_HEADER), "Response Header should contain Credentials Header. origin was");
    }

    private void assertNullAccessControlHeaders(MockHttpServletResponse response) {
        assertNull( response.getHeader(CustomCorsFilter.ALLOW_ORIGIN_HEADER), "Origin Header should not be set");
        assertNull(response.getHeader(CustomCorsFilter.ALLOW_METHODS_HEADER), "Method Header should not be set");
        assertNull(response.getHeader(CustomCorsFilter.ALLOW_HEADERS_HEADER), "Headers Header should not be set");
        assertNull(response.getHeader(CustomCorsFilter.ALLOW_CREDENTIALS_HEADER), "Credentials Header should not be set");
    }

    private MockHttpServletResponse filterWithMockClasses(String origin) throws IOException, ServletException {
        return filterWithMockClasses(origin, false);
    }

    private MockHttpServletResponse filterWithMockClasses(String origin, boolean isPreflight) throws IOException, ServletException {
        // prepare mock classes
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", origin);

        if(isPreflight) {
            request.setMethod("OPTIONS");
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // execute filter
        corsFilter.doFilter(request, response, filterChain);
        return response;
    }

}
