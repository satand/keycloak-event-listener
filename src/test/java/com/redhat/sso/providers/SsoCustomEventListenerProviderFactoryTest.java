package com.redhat.sso.providers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SsoCustomEventListenerProviderFactoryTest {

    SsoCustomEventListenerProviderFactory factory = new SsoCustomEventListenerProviderFactory();

    @Mock
    KeycloakSession session;

    @Test
    void testExeptionOnMissingEnvForCreateProvider() {
        IllegalArgumentException noEnvVar = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            factory.create(session);
        });
        assertThat(noEnvVar.getMessage(), allOf(containsString("The environment variable"), containsString("is mandatory but is not present")));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = "EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS", value = "ldap://localhost:3389"),
            @SetEnvironmentVariable(key = "EXTERNAL_LDAP_SECURITY_PRINCIPAL", value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = "EXTERNAL_LDAP_SECURITY_CREDENTIALS", value = "password"),
            @SetEnvironmentVariable(key = "EXTERNAL_LDAP_USERS_DN", value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = "EXTERNAL_LDAP_ATTRIBUTE_MAP", value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = "EXTERNAL_LDAP_USERNAME_FILTER", value ="cn")
    })
    @Test
    void testCreateProvider() {
        KeycloakTransactionManager keycloakTransactionManager = mock(KeycloakTransactionManager.class);
        when(session.getTransactionManager()).thenReturn(keycloakTransactionManager);

        EventListenerProvider eventListenerProvider = factory.create(session);

        assertThat(eventListenerProvider, notNullValue());
        assertThat(eventListenerProvider, instanceOf(SsoCustomEventListenerProvider.class));
    }

    @Test
    void testExpectedProviderId() {
        assertThat(factory.getId(), equalTo("multiple-ldap-EventListener"));
    }
}