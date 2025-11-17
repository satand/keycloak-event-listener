package com.redhat.sso.service;


import com.redhat.sso.config.ProviderConfig;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.naming.NamingException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private boolean closed = false;
    private final LdapService ldapService;
    private final ProviderConfig config;

    public UserService(LdapService ldapService, ProviderConfig config) {
        this.ldapService = ldapService;
        this.config = config;
    }

    public UserService() {
        this.config = new ProviderConfig();
        this.ldapService = LdapService.getLdapService(config.getProviderUrls(), config.getSecurityPrincipal(), config.getSecurityCredentials());
    }

    public void close() {

        LdapService.close(ldapService);
        closed = true;
    }

    public Map<String, String> queryLDAP(String username) throws NamingException {

        if (closed) {
            LOGGER.warn("Impossible query LDAP: user service is closed");
            return Collections.emptyMap();
        }

        LOGGER.infof("Searching a user with username %s on external LDAP server", username);

        Map<String, String> collect = ldapService.searchUserOnExternalLDAP( 
            config.getUsersDN(), config.getExternalUsernameFilter(), username, config.getExternalAttributes());
        LOGGER.infof("Collected %d attributes for user %s: %s", collect.size(), username, collect);

        return Collections.unmodifiableMap(collect);
    }

    public void updateUser(RealmModel realm, KeycloakSession session, String userId) throws NamingException {

        UserModel newRegisteredUser = Optional.ofNullable(session.userLocalStorage().getUserById(realm, userId))
                .orElseThrow(() -> new IllegalArgumentException(String.format("User with id %s not found", userId)));
        LOGGER.infof("Found user %s on local storage", userId);

        Map<String, String> secondaryAttributes = this.queryLDAP(newRegisteredUser.getUsername());
        secondaryAttributes.forEach(newRegisteredUser::setSingleAttribute);
        this.dumpUser(newRegisteredUser);

        
        // update cache
        if (session.userCache() != null) {
            Optional<UserModel> cachedUser = Optional.ofNullable(session.userCache().getUserById(realm, userId));
            cachedUser.ifPresent(cu -> {
                LOGGER.infof("Updating cache for userId: %s", userId);
                secondaryAttributes.forEach(cu::setSingleAttribute);
            });
        }

    }

    private void dumpUser(UserModel userModel) {

        LOGGER.infof("Username: %s", userModel.getUsername());
        LOGGER.infof("Email: %s", userModel.getEmail());
        LOGGER.infof("FirstName: %s", userModel.getFirstName());
        LOGGER.infof("LastName: %s", userModel.getLastName());
        LOGGER.infof("Actual attributes: %s", userModel.getAttributes());
        LOGGER.infof("From federation: %s", userModel.getFederationLink());
    }
}
