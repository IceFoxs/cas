package org.apereo.cas.web.flow.authentication;

import lombok.val;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link BaseMultifactorAuthenticationProviderEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class BaseMultifactorAuthenticationProviderEventResolver extends AbstractCasWebflowEventResolver
        implements MultifactorAuthenticationProviderResolver {

    
    public BaseMultifactorAuthenticationProviderEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                              final CentralAuthenticationService centralAuthenticationService,
                                                              final ServicesManager servicesManager,
                                                              final TicketRegistrySupport ticketRegistrySupport,
                                                              final CookieGenerator warnCookieGenerator,
                                                              final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                              final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final Collection<String> requestMfaMethod) {
        val providerFound = providers.values()
                .stream()           
                .filter(p -> requestMfaMethod.stream().filter(Objects::nonNull).anyMatch(p::matches))
                .findFirst();
        if (providerFound.isPresent()) {
            val provider = providerFound.get();
            if (provider instanceof VariegatedMultifactorAuthenticationProvider) {
                val multi = VariegatedMultifactorAuthenticationProvider.class.cast(provider);
                return multi.getProviders()
                        .stream()
                        .filter(p -> requestMfaMethod.stream().anyMatch(p::matches))
                        .findFirst();
            }
        }

        return providerFound;
    }

    /**
     * Locate the provider in the collection, and have it match the requested mfa.
     * If the provider is multi-instance, resolve based on inner-registered providers.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final String requestMfaMethod) {
        return resolveProvider(providers, CollectionUtils.wrap(requestMfaMethod));
    }

    @Override
    public Collection<MultifactorAuthenticationProvider> flattenProviders(final Collection<? extends MultifactorAuthenticationProvider> providers) {
        val flattenedProviders = new HashSet<MultifactorAuthenticationProvider>();
        providers.forEach(p -> {
            if (p instanceof VariegatedMultifactorAuthenticationProvider) {
                flattenedProviders.addAll(VariegatedMultifactorAuthenticationProvider.class.cast(p).getProviders());
            } else {
                flattenedProviders.add(p);
            }
        });
        return (Collection) flattenedProviders;
    }

    /**
     * Resolve registered service in request context.
     *
     * @param requestContext the request context
     * @return the registered service
     */
    protected RegisteredService resolveRegisteredServiceInRequestContext(final RequestContext requestContext) {
        val resolvedService = resolveServiceFromAuthenticationRequest(requestContext);
        if (resolvedService != null) {
            val service = this.servicesManager.findServiceBy(resolvedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(resolvedService, service);
            return service;
        }
        LOGGER.debug("Authentication request is not accompanied by a service given none is specified");
        return null;
    }
}
