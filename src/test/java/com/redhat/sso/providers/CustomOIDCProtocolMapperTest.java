package com.redhat.sso.providers;

import com.redhat.sso.config.ProviderConfig;
import com.redhat.sso.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.storage.adapter.InMemoryUserAdapter;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.NamingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SetEnvironmentVariable.SetEnvironmentVariables({
        @SetEnvironmentVariable(key = "EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS", value = "ldap://localhost:3389"),
        @SetEnvironmentVariable(key = "EXTERNAL_LDAP_SECURITY_PRINCIPAL", value = "cn=admin,dc=ldap,dc=example,dc=com"),
        @SetEnvironmentVariable(key = "EXTERNAL_LDAP_SECURITY_CREDENTIALS", value = "password"),
        @SetEnvironmentVariable(key = "EXTERNAL_LDAP_USERS_DN", value = "ou=users,dc=ldap,dc=example,dc=com"),
        @SetEnvironmentVariable(key = "EXTERNAL_LDAP_ATTRIBUTE_MAP", value = "employeeNumber=numero,title=titolo"),
        @SetEnvironmentVariable(key = "EXTERNAL_LDAP_USERNAME_FILTER", value = "cn")
})
class CustomOIDCProtocolMapperTest {

    CustomOIDCProtocolMapper mapper;

    @Mock
    UserService userService;

    @Mock
    KeycloakSession keycloakSession;

    @Mock
    UserSessionModel userSessionModel;

    @Mock
    ClientSessionContext clientSessionContext;

    @BeforeEach
    void init() throws NamingException {
        mapper = new CustomOIDCProtocolMapper(userService);

    }

    @Test
    void testExpectedDisplayType() {
        assertThat(mapper.getDisplayType(), equalTo("Multiple LDAP Claim Mapper"));
    }

    @Test
    void testExpectedId() {
        assertThat(mapper.getId(), equalTo("oidc-multipleldapclaimmapper"));
    }

    @Test
    void testExpectedHelpText() {
        ProviderConfig config = mock(ProviderConfig.class);
        when(userService.getConfig()).thenReturn(config);
        Map<String, String> externalAttributes = new HashMap<>();
        externalAttributes.put("employeeNumber", "numero");
        externalAttributes.put("title", "titolo");
        when(userService.getConfig().getExternalAttributes()).thenReturn(externalAttributes);

        assertThat(mapper.getHelpText(), equalTo("This mapper add all the claims defined in EXTERNAL_LDAP_ATTRIBUTE_MAP environment variable: title => titolo,employeeNumber => numero"));
    }

    @Test
    void testExpectedDisplayCategory() {
        assertThat(mapper.getDisplayCategory(), equalTo(AbstractOIDCProtocolMapper.TOKEN_MAPPER_CATEGORY));
    }

    @Test
    void testExpectedConfigProperties() {
        List<ProviderConfigProperty> configProperties = mapper.getConfigProperties();
        assertThat(configProperties.size(), equalTo(3));
        assertThat(configProperties, hasItem(hasProperty("name", equalTo(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN))));
        assertThat(configProperties, hasItem(hasProperty("name", equalTo(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN))));
        assertThat(configProperties, hasItem(hasProperty("name", equalTo(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO))));
    }

    @Test
    void testCustomClaimsEnabledForAccessToken() throws NamingException {
        AccessToken accessToken = new AccessToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        model.setConfig(config);

        prepareContext();

        AccessToken actualToken = mapper.transformAccessToken(accessToken, model, keycloakSession, userSessionModel, clientSessionContext);
        assertThat(actualToken.getOtherClaims().size(), equalTo(2));
        assertThat(actualToken.getOtherClaims().get("titolo"), equalTo("NormalUser"));
        assertThat(actualToken.getOtherClaims().get("numero"), equalTo("42"));
    }

    @Test
    void testCustomClaimsDisabledForAccessToken() {
        AccessToken accessToken = new AccessToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "false");
        model.setConfig(config);

        AccessToken actualToken = mapper.transformAccessToken(accessToken, model, keycloakSession, userSessionModel, clientSessionContext);
        assertThat(actualToken.getOtherClaims().size(), equalTo(0));

    }


    @Test
    void testCustomClaimsEnabledForIDToken() throws NamingException {
        IDToken idToken = new IDToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        model.setConfig(config);

        prepareContext();

        IDToken actualToken = mapper.transformIDToken(idToken, model, keycloakSession, userSessionModel, clientSessionContext);
        assertThat(actualToken.getOtherClaims().size(), equalTo(2));
        assertThat(actualToken.getOtherClaims().get("titolo"), equalTo("NormalUser"));
        assertThat(actualToken.getOtherClaims().get("numero"), equalTo("42"));
    }

    @Test
    void testCustomClaimsDisabledForIDToken() {
        IDToken idToken = new IDToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "false");
        model.setConfig(config);

        IDToken actualToken = mapper.transformIDToken(idToken, model, keycloakSession, userSessionModel, clientSessionContext);
        assertThat(actualToken.getOtherClaims().size(), equalTo(0));

    }

    @Test
    void testCustomClaimsEnabledForUserInfo() throws NamingException {
        AccessToken accessToken = new AccessToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "true");
        model.setConfig(config);

        prepareContext();

        AccessToken actualToken = mapper.transformUserInfoToken(accessToken, model, keycloakSession, userSessionModel, clientSessionContext);
        assertThat(actualToken.getOtherClaims().size(), equalTo(2));
        assertThat(actualToken.getOtherClaims().get("titolo"), equalTo("NormalUser"));
        assertThat(actualToken.getOtherClaims().get("numero"), equalTo("42"));
    }

    @Test
    void testCustomClaimsDisabledForUserInfo() {
        AccessToken accessToken = new AccessToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "false");
        model.setConfig(config);

        AccessToken actualToken = mapper.transformUserInfoToken(accessToken, model, keycloakSession, userSessionModel, clientSessionContext);
        assertThat(actualToken.getOtherClaims().size(), equalTo(0));

    }

    @Test
    void testExceptionOnClaimCompilation() throws NamingException {
        IDToken idToken = new IDToken();
        ProtocolMapperModel model = new ProtocolMapperModel();

        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "true");
        model.setConfig(config);

        prepareWrongContext();

        IllegalArgumentException exceptionOnAttribute = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            mapper.transformIDToken(idToken, model, keycloakSession, userSessionModel, clientSessionContext);
        });
        assertThat(exceptionOnAttribute.getMessage(), equalTo("Error reading attributes"));

    }


    private void prepareContext() throws NamingException {

        Map<String, String> attributes = new HashMap<>();
        attributes.put("titolo", "NormalUser");
        attributes.put("numero", "42");

        RealmModel realmModel = mock(RealmModel.class);
        UserModel userModel = new InMemoryUserAdapter(keycloakSession, realmModel, "mario.rossi");

        userModel.setFirstName("Mario");
        userModel.setLastName("Rossi");
        userModel.setUsername("mario.rossi");
        userModel.setEmail("mario.rossi@example.com");
        userModel = spy(userModel);

        when(userService.queryLDAP(eq("mario.rossi"))).thenReturn(attributes);

        when(userSessionModel.getUser()).thenReturn(userModel);
    }

    private void prepareWrongContext() throws NamingException {


        RealmModel realmModel = mock(RealmModel.class);
        UserModel userModel = new InMemoryUserAdapter(keycloakSession, realmModel, "mario.rossi");

        userModel.setFirstName("Mario");
        userModel.setLastName("Rossi");
        userModel.setUsername("mario.rossi");
        userModel.setEmail("mario.rossi@example.com");
        userModel = spy(userModel);

        when(userService.queryLDAP(any())).thenThrow(new NamingException("Exception on attribute"));

        when(userSessionModel.getUser()).thenReturn(userModel);
    }


}