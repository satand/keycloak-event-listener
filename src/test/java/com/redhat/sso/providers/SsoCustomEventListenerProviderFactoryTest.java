package com.redhat.sso.providers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.redhat.sso.config.ProviderConfig;
import com.redhat.sso.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@SetEnvironmentVariable.SetEnvironmentVariables({
    @SetEnvironmentVariable(key = "EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS", value = "ldap://localhost:3389"),
    @SetEnvironmentVariable(key = "EXTERNAL_LDAP_SECURITY_PRINCIPAL", value = "cn=admin,dc=ldap,dc=example,dc=com"),
    @SetEnvironmentVariable(key = "EXTERNAL_LDAP_SECURITY_CREDENTIALS", value = "password"),
    @SetEnvironmentVariable(key = "EXTERNAL_LDAP_USERS_DN", value = "ou=users,dc=ldap,dc=example,dc=com"),
    @SetEnvironmentVariable(key = "EXTERNAL_LDAP_ATTRIBUTE_MAP", value = "employeeNumber=numero,title=titolo"),
    @SetEnvironmentVariable(key = "EXTERNAL_LDAP_USERNAME_FILTER", value ="cn")
})
@ExtendWith(MockitoExtension.class)
class SsoCustomEventListenerProviderFactoryTest {

    @Mock
    KeycloakSession session;

    @Mock
    UserService userService;

    @ClearEnvironmentVariable.ClearEnvironmentVariables({
        @ClearEnvironmentVariable(key = "EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS")
    })
    @Test
    void testExeptionOnMissingEnvForCreateEventListenerProviderFactory() {
        IllegalArgumentException noEnvVar = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            
            new SsoCustomEventListenerProviderFactory(userService);
        });

        assertThat(noEnvVar.getMessage(), allOf(containsString("The environment variable"), containsString("is mandatory but is not present")));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_EVENT_LISTENER_ENABLED, value = "FALSE"),
    })
    @Test
    void testCreateEventListenerProviderWhenDisabled() {
        SsoCustomEventListenerProviderFactory factory = new SsoCustomEventListenerProviderFactory(userService);

        EventListenerProvider eventListenerProvider = factory.create(session);

        assertThat(eventListenerProvider, notNullValue());
        assertThat(eventListenerProvider, instanceOf(SsoCustomEventListenerProviderFactory.NoOpSsoCustomEventListenerProvider.class));
    }


    @SetEnvironmentVariable.SetEnvironmentVariables({
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_EVENT_LISTENER_ENABLED, value = "TRUE"),
    })
    @Test
    void testCreateEventListenerProviderWhenEnabled() {
        SsoCustomEventListenerProviderFactory factory = new SsoCustomEventListenerProviderFactory(userService);


        KeycloakTransactionManager keycloakTransactionManager = mock(KeycloakTransactionManager.class);
        when(session.getTransactionManager()).thenReturn(keycloakTransactionManager);

        EventListenerProvider eventListenerProvider = factory.create(session);

        assertThat(eventListenerProvider, notNullValue());
        assertThat(eventListenerProvider, instanceOf(SsoCustomEventListenerProvider.class));
    }

    @Test
    void testCreateEventListenerProviderWhenEnabledByDefault() {
        SsoCustomEventListenerProviderFactory factory = new SsoCustomEventListenerProviderFactory(userService);


        KeycloakTransactionManager keycloakTransactionManager = mock(KeycloakTransactionManager.class);
        when(session.getTransactionManager()).thenReturn(keycloakTransactionManager);

        EventListenerProvider eventListenerProvider = factory.create(session);

        assertThat(eventListenerProvider, notNullValue());
        assertThat(eventListenerProvider, instanceOf(SsoCustomEventListenerProvider.class));
    }

    @Test
    void testExpectedEventListenerProviderFactoryId() {
        SsoCustomEventListenerProviderFactory factory = new SsoCustomEventListenerProviderFactory(userService);

        assertThat(factory.getId(), equalTo("multiple-ldap-EventListener"));
    }
}