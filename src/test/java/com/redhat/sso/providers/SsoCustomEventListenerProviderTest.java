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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.naming.NamingException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SsoCustomEventListenerProviderTest {

    @Mock
    KeycloakSession keycloakSession;

    @Mock
    UserService userService;

    SsoCustomEventListenerProvider provider;

    @BeforeEach
    void init() throws NamingException {

        KeycloakTransactionManager keycloakTransactionManager = mock(KeycloakTransactionManager.class);

        when(keycloakSession.getTransactionManager()).thenReturn(keycloakTransactionManager);

        doNothing().when(keycloakTransactionManager).enlistPrepare(any());

        provider = new SsoCustomEventListenerProvider(keycloakSession, userService);
    }

    @Test
    void testOnLoginEvent() throws Exception {

        RealmProvider realmProvider =  mock(RealmProvider.class);
        RealmModel realmModel =  mock(RealmModel.class);

        when(keycloakSession.realms()).thenReturn(realmProvider);
        when(realmProvider.getRealm(anyString())).thenReturn(realmModel);

        Event loginEvent = new Event();
        loginEvent.setType(EventType.LOGIN);
        loginEvent.setRealmId("rhsso.EventListener-v1");
        loginEvent.setUserId("mario.rossi");

        provider.handleClientEvent(loginEvent);

        verify(userService,times(1)).updateUser(any(),any(),eq(loginEvent.getUserId()));
    }

    @Test
    void testOnImpersonateEvent() throws Exception {

        RealmProvider realmProvider =  mock(RealmProvider.class);
        RealmModel realmModel =  mock(RealmModel.class);

        when(keycloakSession.realms()).thenReturn(realmProvider);
        when(realmProvider.getRealm(anyString())).thenReturn(realmModel);

        Event impersonateEvent = new Event();
        impersonateEvent.setType(EventType.IMPERSONATE);
        impersonateEvent.setRealmId("rhsso.EventListener-v1");
        impersonateEvent.setUserId("mario.rossi");

        provider.handleClientEvent(impersonateEvent);

        verify(userService,times(1)).updateUser(any(),any(),eq(impersonateEvent.getUserId()));
    }

    @Test
    void testNoActionOnOtherEvents() throws NamingException {
        Event loginEvent = new Event();
        loginEvent.setType(EventType.REGISTER);
        loginEvent.setRealmId("rhsso.EventListener-v1");
        loginEvent.setUserId("mario.rossi");

        provider.handleClientEvent(loginEvent);

        verify(userService,times(0)).updateUser(any(),any(),eq(loginEvent.getUserId()));
    }

    @Test
    void testExceptionOnEventHandling() throws NamingException {

        RealmProvider realmProvider =  mock(RealmProvider.class);
        RealmModel realmModel =  mock(RealmModel.class);

        when(keycloakSession.realms()).thenReturn(realmProvider);
        when(realmProvider.getRealm(anyString())).thenReturn(realmModel);
        
        Event loginEvent = new Event();
        loginEvent.setType(EventType.LOGIN);
        loginEvent.setRealmId("rhsso.EventListener-v1");
        loginEvent.setUserId("this_user_does_not_exists");

        doThrow(RuntimeException.class).when(userService).updateUser(any(),any(), eq(loginEvent.getUserId()));

        provider.handleClientEvent(loginEvent); // here we are testing how we have correctly managed the thrown exceptions not exiting with error

        verify(userService,times(1)).updateUser(any(),any(),eq(loginEvent.getUserId()));
    }

}