package com.redhat.sso.providers;

import com.redhat.sso.config.ProviderConfig;
import com.redhat.sso.service.UserService;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SsoCustomEventListenerProviderFactory implements EventListenerProviderFactory {

    public static final class NoOpSsoCustomEventListenerProvider implements EventListenerProvider {

        @Override
        public void close() {
            //Nothing to do
        }

        @Override
        public void onEvent(Event event) {
            //Nothing to do
        }

        @Override
        public void onEvent(AdminEvent event, boolean includeRepresentation) {
            //Nothing to do
        }
    }

    private static final Logger LOGGER = Logger.getLogger(SsoCustomEventListenerProviderFactory.class.getName());

    private static final NoOpSsoCustomEventListenerProvider NO_OP_PROVIDER = new NoOpSsoCustomEventListenerProvider();

    private final ProviderConfig config;
    private final UserService userService;

    public SsoCustomEventListenerProviderFactory(UserService userService) {

        this.config = new ProviderConfig();
        this.userService = userService;
    }

    public SsoCustomEventListenerProviderFactory() {

        this(new UserService());
    }

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {

        if (config.isEventListenerEnabled()) {

            return new SsoCustomEventListenerProvider(keycloakSession, userService);
        } else {

            LOGGER.warnf("The event listener is disabled. If you want to enable it, change the %s env property value.", ProviderConfig.EXTERNAL_LDAP_FEDERATION_EVENT_LISTENER_ENABLED);
            return NO_OP_PROVIDER;
        }
    }

    @Override
    public void init(Config.Scope scope) {
        //Not useful
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        //Not useful
    }

    @Override
    public void close() {
        //Not useful
    }

    @Override
    public String getId() {
        return "multiple-ldap-EventListener";
    }
}
