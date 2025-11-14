package com.redhat.sso.providers;

import com.redhat.sso.config.ProviderConfig;
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
    private final ProviderConfig config;

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomOIDCProtocolMapper.class);
    }

    public CustomOIDCProtocolMapper() {

        this.userService = new UserService();
        this.config = new ProviderConfig();
    }

    protected CustomOIDCProtocolMapper(UserService userService, ProviderConfig config) {

        this.userService = userService;
        this.config = config;
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
        config.getExternalAttributes().forEach((k, v) -> {
            sb.append(sb.length() == 0 ? "" : ",").append(k).append(" => ").append(v);
        });

        return String.format("This mapper add all the claims defined in EXTERNAL_LDAP_ATTRIBUTE_MAP environment variable: %s", sb.toString());
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {

        if (!config.isMapperEnabled()) {

            LOGGER.warnf("The mapper is disabled. If you want to enable it, change the %s env property value.", ProviderConfig.EXTERNAL_LDAP_FEDERATION_MAPPER_ENABLED);
            return;
        }

        enrichToken(token, userSession.getUser().getUsername());
    }

    private void enrichToken(JsonWebToken token, String username) {
        try {
            Map<String, String> customAttributes = userService.queryLDAP(username);
            Map<String, Object> otherClaims = token.getOtherClaims();
            customAttributes.forEach(otherClaims::put);
        } catch (NamingException e) {

            throw new IllegalArgumentException("Error reading attributes", e);
        }
    }

}