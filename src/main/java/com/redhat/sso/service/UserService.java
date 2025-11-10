package com.redhat.sso.service;


import com.redhat.sso.config.ProviderConfig;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private final LdapService ldapService;
    private final ProviderConfig config;

    public UserService(LdapService ldapService, ProviderConfig config) {
        this.ldapService = ldapService;
        this.config = config;
    }

    public UserService() {
        this.ldapService = new LdapService();
        this.config = new ProviderConfig();
    }

    public ProviderConfig getConfig() {
        return config;
    }

    public Map<String, String> queryLDAP(String username) throws NamingException {

        LOGGER.log(Logger.Level.INFO, "Searching on external LDAP server");

        DirContext context = ldapService.initContext(config.getProviderUrl(), config.getSecurityPrincipal(), config.getSecurityCredentials());

        LOGGER.log(Logger.Level.INFO, "External LDAP context initialized ");

        Map<String, String> collect = ldapService.searchUserOnExternalLDAP(context, config.getUsersDN(), config.getExternalUsernameFilter(), username, config.getExternalAttributes());

        LOGGER.log(Logger.Level.INFO, String.format("Collected %d attributes for user %s: %s", collect.size(), username, collect));
        context.close();

        return Collections.unmodifiableMap(collect);

    }

    public void updateUser(RealmModel realm, KeycloakSession session, String userId) throws NamingException {

        UserModel newRegisteredUser = Optional.ofNullable(session.userLocalStorage().getUserById(realm, userId))
                .orElseThrow(() -> new IllegalArgumentException(String.format("User with id %s not found", userId)));
        LOGGER.infof("Found user %s on local storage", newRegisteredUser.getUsername());

        Map<String, String> secondaryAttributes = this.queryLDAP(newRegisteredUser.getUsername());
        secondaryAttributes.forEach(newRegisteredUser::setSingleAttribute);
        this.dumpUser(newRegisteredUser);
        // update cache
        if (session.userCache() != null) {
            LOGGER.infof("Updating cache for userId: %s", userId);
            UserModel cachedUser = session.userCache().getUserById(realm, userId);
            secondaryAttributes.forEach(cachedUser::setSingleAttribute);
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
