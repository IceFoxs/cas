package org.apereo.cas.authentication;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogatePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@Slf4j
public class SurrogatePrincipalResolverTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void verifyResolverDefault() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals(p.getId(), credential.getId());
    }

    @Test
    public void verifyResolverAttribute() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val p = resolver.resolve(credential);
        assertNotNull(p);
        assertTrue(p.getId().equals("TEST"));
    }

    @Test
    public void verifyResolverSurrogateWithoutPrincipal() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        val credential = new SurrogateUsernamePasswordCredential();
        thrown.expect(IllegalArgumentException.class);
        resolver.resolve(credential);
    }
    
    @Test
    public void verifyResolverSurrogate() {
        val resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        val credential = new SurrogateUsernamePasswordCredential();
        credential.setSurrogateUsername("surrogate");
        credential.setUsername("username");
        val p = resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal("casuser")),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()));
        assertNotNull(p);
        assertTrue(p.getId().equals("casuser"));
    }
}
