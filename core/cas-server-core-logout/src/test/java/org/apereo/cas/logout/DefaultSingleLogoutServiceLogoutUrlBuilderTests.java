package org.apereo.cas.logout;

import lombok.val;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DefaultSingleLogoutServiceLogoutUrlBuilderTests {

    @Test
    public void verifyLogoutUrlByService() throws Exception {
        val svc = getRegisteredService("https://www.google.com");
        svc.setLogoutUrl(new URL("http://www.example.com/logout"));
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("https://www.google.com"));
        assertEquals(url.iterator().next(), svc.getLogoutUrl());
    }

    @Test
    public void verifyLogoutUrlByDefault() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("https://www.somewhere.com/logout?p=v"));
        assertEquals(url.iterator().next(), new URL("https://www.somewhere.com/logout?p=v"));
    }

    @Test
    public void verifyLogoutUrlUnknownUrlProtocol() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("imaps://etc.example.org"));
        assertTrue(url.isEmpty());
    }

    @Test
    public void verifyLocalLogoutUrlWithLocalUrlNotAllowed() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertTrue(url.isEmpty());
    }

    @Test
    public void verifyLocalLogoutUrlWithLocalUrlAllowed() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(url.iterator().next(), new URL("https://localhost/logout?p=v"));
    }

    @Test
    public void verifyLocalLogoutUrlWithValidRegExValidationAndLocalUrlNotAllowed() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\w*", true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(url.iterator().next(), new URL("https://localhost/logout?p=v"));
    }

    @Test
    public void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlAllowed() throws Exception {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(true, "\\d*", true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertEquals(url.iterator().next(), new URL("https://localhost/logout?p=v"));
    }

    @Test
    public void verifyLocalLogoutUrlWithInvalidRegExValidationAndLocalUrlNotAllowed() {
        val svc = getRegisteredService(".+");
        svc.setLogoutUrl(null);
        val builder = createDefaultSingleLogoutServiceLogoutUrlBuilder(false, "\\d*", true);
        val url = builder.determineLogoutUrl(svc, getService("https://localhost/logout?p=v"));
        assertTrue(url.isEmpty());
    }

    private DefaultSingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls) {
        return createDefaultSingleLogoutServiceLogoutUrlBuilder(allowLocalLogoutUrls, null, true);
    }

    private DefaultSingleLogoutServiceLogoutUrlBuilder createDefaultSingleLogoutServiceLogoutUrlBuilder(final boolean allowLocalLogoutUrls,
                                                                                                        final String authorityValidationRegEx,
                                                                                                        final boolean authorityValidationRegExCaseSensitive) {
        val validator = new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx,
            authorityValidationRegExCaseSensitive).getObject();
        return new DefaultSingleLogoutServiceLogoutUrlBuilder(validator);
    }

    @SneakyThrows
    public static AbstractRegisteredService getRegisteredService(final String id) {
        val s = new RegexRegisteredService();
        s.setServiceId(id);
        s.setName("Test service " + id);
        s.setDescription("Registered service description");
        s.setProxyPolicy(new RegexMatchingRegisteredServiceProxyPolicy("^https?://.+"));
        s.setId(RandomUtils.getNativeInstance().nextInt(Math.abs(s.hashCode())));
        return s;
    }

    public static AbstractWebApplicationService getService(final String url) {
        val request = new MockHttpServletRequest();
        request.addParameter("service", url);
        return (AbstractWebApplicationService) new WebApplicationServiceFactory().createService(request);
    }
}
