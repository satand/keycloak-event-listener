package com.redhat.sso.service;

import com.redhat.sso.config.ProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.cache.UserCache;
import org.keycloak.storage.adapter.InMemoryUserAdapter;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    UserService userService;

    @Mock
    LdapService ldapService;

    @Mock
    ProviderConfig providerConfig;

    @BeforeEach
    void init() {
        userService = new UserService(ldapService, providerConfig);
    }

    private void initLdapMocks() throws NamingException {
        DirContext ctx = mock(DirContext.class);
        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("titolo", "Developer");
        attributeMapping.put("numero", "42");

        when(ldapService.initContext(any(), any(), any())).thenReturn(ctx);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(3, String.class).equals("mario.rossi")) {
                return attributeMapping;
            }
            throw new IllegalArgumentException("user user_not_exists not found");
        }).when(ldapService).searchUserOnExternalLDAP(eq(ctx), any(), any(), any(), any());
    }

    @Test
    void testSameConfig(){
        assertThat(userService.getConfig(),equalTo(providerConfig));
    }

    @Test
    void testQueryOK() throws NamingException {

        // Given
        initLdapMocks();

        Map<String, String> actualResult = userService.queryLDAP("mario.rossi");

        assertThat(actualResult.size(), equalTo(2));
        assertThat(actualResult.get("titolo"), equalTo("Developer"));
        assertThat(actualResult.get("numero"), equalTo("42"));

    }

    @Test
    void testQueryUserNotFound() throws NamingException {

        // Given
        initLdapMocks();

        IllegalArgumentException userNotFound = assertThrows(IllegalArgumentException.class, () -> {
            userService.queryLDAP("user_not_exists");
        });

        assertThat(userNotFound.getMessage(), equalTo("user user_not_exists not found"));
    }

    @Test
    void testUpdatedUser() throws NamingException {

        RealmModel realmModel = mock(RealmModel.class);
        KeycloakSession keycloakSession = mock(KeycloakSession.class);

        UserCache userCache = mock(UserCache.class);
        UserProvider userProvider = mock(UserProvider.class);
        // Session
        when(keycloakSession.userLocalStorage()).thenReturn(userProvider);
        when(keycloakSession.userCache()).thenReturn(userCache);

        UserModel userModel = new InMemoryUserAdapter(keycloakSession, realmModel, "mario.rossi");

        userModel.setFirstName("Mario");
        userModel.setLastName("Rossi");
        userModel.setUsername("mario.rossi");
        userModel.setEmail("mario.rossi@example.com");
        userModel = spy(userModel);

        // User found on cache and on provider
        when(userCache.getUserById(eq(realmModel), eq("mario.rossi"))).thenReturn(userModel);
        when(userProvider.getUserById(eq(realmModel), eq("mario.rossi"))).thenReturn(userModel);

        // Given
        initLdapMocks();

        userService.updateUser(realmModel, keycloakSession, "mario.rossi");

        verify(userModel, times(2)).setSingleAttribute(eq("titolo"), any());
        verify(userModel, times(2)).setSingleAttribute(eq("numero"), any());
        verify(userCache, times(1)).getUserById(eq(realmModel), eq("mario.rossi"));
    }

    @Test
    void testUpdatedUserWhenNotFoundInCache() throws NamingException {

        RealmModel realmModel = mock(RealmModel.class);
        KeycloakSession keycloakSession = mock(KeycloakSession.class);

        UserCache userCache = mock(UserCache.class);
        UserProvider userProvider = mock(UserProvider.class);
        // Session
        when(keycloakSession.userLocalStorage()).thenReturn(userProvider);
        when(keycloakSession.userCache()).thenReturn(userCache);

        UserModel userModel = new InMemoryUserAdapter(keycloakSession, realmModel, "mario.rossi");

        userModel.setFirstName("Mario");
        userModel.setLastName("Rossi");
        userModel.setUsername("mario.rossi");
        userModel.setEmail("mario.rossi@example.com");
        userModel = spy(userModel);

        // User found on provider
        when(userProvider.getUserById(eq(realmModel), eq("mario.rossi"))).thenReturn(userModel);
        // User not found on cache
        when(userCache.getUserById(eq(realmModel), eq("mario.rossi"))).thenReturn(null);

        // Given
        initLdapMocks();

        userService.updateUser(realmModel, keycloakSession, "mario.rossi");

        verify(userModel, times(1)).setSingleAttribute(eq("titolo"), any());
        verify(userModel, times(1)).setSingleAttribute(eq("numero"), any());
        verify(userCache, times(1)).getUserById(eq(realmModel), eq("mario.rossi"));
    }

    @Test
    void testUpdatedUserNoCache() throws NamingException {

        RealmModel realmModel = mock(RealmModel.class);
        KeycloakSession keycloakSession = mock(KeycloakSession.class);

        UserProvider userProvider = mock(UserProvider.class);
        // Session
        when(keycloakSession.userLocalStorage()).thenReturn(userProvider);
        // no cache
        when(keycloakSession.userCache()).thenReturn(null);

        UserModel userModel = new InMemoryUserAdapter(keycloakSession, realmModel, "mario.rossi");

        userModel.setFirstName("Mario");
        userModel.setLastName("Rossi");
        userModel.setUsername("mario.rossi");
        userModel.setEmail("mario.rossi@example.com");
        userModel = spy(userModel);

        // User found on cache and on provider
        when(userProvider.getUserById(eq(realmModel), eq("mario.rossi"))).thenReturn(userModel);

        // Given
        initLdapMocks();

        userService.updateUser(realmModel, keycloakSession, "mario.rossi");

        verify(userModel, times(1)).setSingleAttribute(eq("titolo"), any());
        verify(userModel, times(1)).setSingleAttribute(eq("numero"), any());
    }

    @Test
    void testUpdateNotFoundUser() {
        RealmModel realmModel = mock(RealmModel.class);
        KeycloakSession keycloakSession = mock(KeycloakSession.class);

        UserProvider userProvider = mock(UserProvider.class);
        // Session
        when(keycloakSession.userLocalStorage()).thenReturn(userProvider);
        // User found on cache and on provider
        when(userProvider.getUserById(eq(realmModel), any())).thenReturn(null);
        IllegalArgumentException userNotFound = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(realmModel, keycloakSession, "non.existent");
        });

        assertThat(userNotFound.getMessage(), equalTo("User with id non.existent not found"));


    }


}