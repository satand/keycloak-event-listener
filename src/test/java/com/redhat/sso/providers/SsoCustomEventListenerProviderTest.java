package com.redhat.sso.providers;

import com.redhat.sso.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SsoCustomEventListenerProviderTest {

    private static final Logger log = LoggerFactory.getLogger(SsoCustomEventListenerProviderTest.class);

    @Mock
    KeycloakSession keycloakSession;

    @Mock
    UserService userService;

    @Mock
    RealmProvider realmProvider;

    @Mock
    RealmModel realmModel;

    SsoCustomEventListenerProvider provider;

    @BeforeEach
    void init() throws NamingException {


        when(keycloakSession.realms()).thenReturn(realmProvider);

        KeycloakTransactionManager keycloakTransactionManager = mock(KeycloakTransactionManager.class);

        when(keycloakSession.getTransactionManager()).thenReturn(keycloakTransactionManager);

        doNothing().when(keycloakTransactionManager).enlistPrepare(any());


        provider = new SsoCustomEventListenerProvider(keycloakSession, userService);


    }

    @Test
    void testOnLoginEvent() throws Exception {
        Event loginEvent = new Event();
        loginEvent.setType(EventType.LOGIN);
        loginEvent.setRealmId("rhsso.EventListener-v1");
        loginEvent.setUserId("mario.rossi");

        provider.handleClientEvent(loginEvent);

        verify(userService,times(1)).updateUser(any(),any(),eq("mario.rossi"));

    }

    @Test
    void testNoActionOnOtherEvents() throws NamingException {
        Event loginEvent = new Event();
        loginEvent.setType(EventType.REGISTER);
        loginEvent.setRealmId("rhsso.EventListener-v1");
        loginEvent.setUserId("mario.rossi");

        provider.handleClientEvent(loginEvent);
        verify(userService,times(0)).updateUser(any(),any(),eq("mario.rossi"));


    }

    @Test
    void testExceptionOnEventHandling() throws NamingException {
        Event loginEvent = new Event();
        loginEvent.setType(EventType.LOGIN);
        loginEvent.setRealmId("rhsso.EventListener-v1");
        loginEvent.setUserId("this_user_does_not_exists");

        provider.handleClientEvent(loginEvent);

        // no call to these, the exception is logged
        verify(userService, times(0)).queryLDAP(any());
    }

}