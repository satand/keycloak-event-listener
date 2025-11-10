package com.redhat.sso.providers;

import com.redhat.sso.service.UserService;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * @author Luciano Di Leonardo
 * @email ldileona@redhat.com
 * @date 24 Apr 2023
 * @company Red Hat inc.
 * @role Architect
 */
public class SsoCustomEventListenerProviderFactory implements EventListenerProviderFactory {

    @Override
    public SsoCustomEventListenerProvider create(KeycloakSession keycloakSession) {
        return new SsoCustomEventListenerProvider(keycloakSession, new UserService());
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
