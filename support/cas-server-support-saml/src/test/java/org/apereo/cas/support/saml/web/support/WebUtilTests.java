package org.apereo.cas.support.saml.web.support;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
public class WebUtilTests {

    @Test
    public void verifyFindService() {
        val casArgumentExtractor =
            new DefaultArgumentExtractor(new WebApplicationServiceFactory());
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");

        val service = HttpRequestUtils.getService(Arrays.asList(casArgumentExtractor), request);

        assertNotNull(service);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyFoundNoService() {
        val casArgumentExtractor = new DefaultArgumentExtractor(new SamlServiceFactory(null));
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        val service = HttpRequestUtils.getService(Collections.singletonList(casArgumentExtractor), request);
        assertNull(service);
    }
}
