package com.redhat.sso.providers;

import com.redhat.sso.service.UserService;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomOIDCProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {
    private static final Logger LOGGER = Logger.getLogger(CustomOIDCProtocolMapper.class.getName());


    public static final String PROVIDER_ID = "oidc-multipleldapclaimmapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    private final UserService userService;

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomOIDCProtocolMapper.class);
    }

    public CustomOIDCProtocolMapper() {
        this.userService = new UserService();
    }

    protected CustomOIDCProtocolMapper(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getDisplayType() {
        return "Multiple LDAP Claim Mapper";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {

        StringBuilder sb = new StringBuilder();
        userService.getConfig().getExternalAttributes().forEach((k, v) -> {
            sb.append(sb.length() == 0 ? "" : ",").append(k).append(" => ").append(v);
        });

        return String.format("This mapper add all the claims defined in EXTERNAL_LDAP_ATTRIBUTE_MAP environment variable: %s", sb.toString());
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession keycloakSession,
                                            UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        boolean updateNeeded = OIDCAttributeMapperHelper.includeInAccessToken(mappingModel);
        LOGGER.debugf("Are custom claims included in access token? %s", updateNeeded);
        if (updateNeeded) {
            return updateToken(token, mappingModel, keycloakSession, userSession, clientSessionCtx);
        }
        return super.transformAccessToken(token, mappingModel, keycloakSession, userSession, clientSessionCtx);

    }

    @Override
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel, KeycloakSession keycloakSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        boolean updateNeeded = OIDCAttributeMapperHelper.includeInIDToken(mappingModel);
        LOGGER.debugf("Are custom claims included in IDtoken? %s", updateNeeded);
        if (updateNeeded) {
            return updateToken(token, mappingModel, keycloakSession, userSession, clientSessionCtx);
        }
        return super.transformIDToken(token, mappingModel, keycloakSession, userSession, clientSessionCtx);

    }

    @Override
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession keycloakSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        boolean updateNeeded = OIDCAttributeMapperHelper.includeInUserInfo(mappingModel);
        LOGGER.debugf("Are custom claims included in Userinfo? %s", updateNeeded);
        if (updateNeeded) {
            return updateToken(token, mappingModel, keycloakSession, userSession, clientSessionCtx);
        }
        return super.transformUserInfoToken(token, mappingModel, keycloakSession, userSession, clientSessionCtx);

    }

    private <T extends IDToken> T updateToken(T token, ProtocolMapperModel mappingModel, KeycloakSession keycloakSession, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        compileToken(token, userSession.getUser().getUsername());
        setClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);
        return token;
    }

    private void compileToken(JsonWebToken token, String username) {
        try {
            Map<String, String> customAttributes = userService.queryLDAP(username);
            customAttributes.forEach((key, value) -> token.getOtherClaims().put(key, value));
        } catch (NamingException e) {
            throw new IllegalArgumentException("Error reading attributes", e);
        }

    }

}