package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import lombok.val;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;

/**
 * This is {@link SamlProfileSaml2ResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(FileSystemCategory.class)
public class SamlProfileSaml2ResponseBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifySamlResponseAllSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true);
        val adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAllSignedEncrypted() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, true, true);
        val adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAssertionSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, true);
        val adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseResponseSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(true, false);
        val adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseNothingSigned() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val service = getSamlRegisteredServiceForTestShib(false, false);
        val adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        val authnRequest = getAuthnRequestFor(service);
        val assertion = getAssertion();

        val samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }
}
