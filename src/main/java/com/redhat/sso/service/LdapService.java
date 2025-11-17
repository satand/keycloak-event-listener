package com.redhat.sso.service;

import org.jboss.logging.Logger;

import com.redhat.sso.utils.HashUtils;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LdapService {

    public static class LdapCtxCreationException extends RuntimeException {
    
        public LdapCtxCreationException(Exception cause) {

            super("Error creating the LDAP context: " + cause.getMessage(), cause);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(LdapService.class.getName());

    private static Map<String,LdapService> INSTANCE_MAP = new HashMap<>();

    private final DirContext ctx;

    LdapService(List<String> providerUrls, String securityPrincipal, String securityCredentials) {

        try {
            ctx = initContext(providerUrls, securityPrincipal, securityCredentials);
            LOGGER.info("LDAP Context initialized");
        } catch (NamingException e) {
            throw new LdapCtxCreationException(e);
        }
    }

    DirContext initContext(List<String> providerUrls, String securityPrincipal, String securityCredentials) throws NamingException {

        Hashtable<String, String> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrls.stream().reduce("", (a, b) -> a + " " + b));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "50");
        env.put("com.sun.jndi.ldap.connect.pool.prefsize", "5");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "300000"); // 5 mins
        env.put("com.sun.jndi.ldap.connect.timeout", "5000"); // 5 secs

        return new InitialDirContext(env);
    }

    public static synchronized LdapService getLdapService(List<String> providerUrls, String securityPrincipal, String securityCredentials) {

        String sha256 = HashUtils.toSha256(providerUrls.stream().collect(Collectors.joining()) + securityPrincipal + securityCredentials);
        
        LdapService service = INSTANCE_MAP.get(sha256);
        if (service == null) {
            
            service = new LdapService(providerUrls, securityPrincipal, securityCredentials);
            INSTANCE_MAP.put(sha256, service);
        }

        return service;
    }

    public static void close(LdapService service) {
        if (service == null) {
            return;
        }

        service.close();

        INSTANCE_MAP.entrySet().removeIf(entry -> entry.getValue() == service);
    }

    public void close() {

        try {
            ctx.close();
        } catch (NamingException e) {
            LOGGER.warn("Error closing the LDAP context: " + e.getMessage(), e);
        }
    }

    /**
     * Search User directly with baseDN and (uniqueAttribute=userName) as filter
     *
     * @param baseDN           baseDN for the search
     * @param uniqueAttribute  attribute used on LDAP to filter user (usually cn or samaccountname)
     * @param userName         SSO username
     * @param attributeMapping map of attributes names to retrieve and how to map them to user
     * @return Map of key, value attributes found
     * @throws NamingException when the search went wrong
     */
    public Map<String, String> searchUserOnExternalLDAP(String baseDN, String uniqueAttribute, String userName, Map<String, String> attributeMapping) throws NamingException {
        String filter = String.format("(%s=%s)", uniqueAttribute, userName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> search = ctx.search(baseDN, filter, searchControls);
        ArrayList<SearchResult> list = Collections.list(search);

        if (list.size() != 1) {
            throw new IllegalArgumentException(String.format("Found %s record(s) using baseDN %s and filter %s. Expected 1", list.size(), baseDN, filter));
        }
        SearchResult userFound = list.stream().findFirst().get();

        final Attributes userAttributes = userFound.getAttributes();

        // key1=value1,key2=value2,... -> value1=userAttributes.get(key1),value2=userAttributes.get(key2),...
        Map<String, String> result = new HashMap<>();
        attributeMapping.forEach((key, value) -> {
            try {
                Attribute attribute = userAttributes.get(key);
                if (attribute != null && attribute.get() != null) {
                    result.put(value, (String) attribute.get());
                } else {
                    LOGGER.warnf("Attribute %s not found in %s using search filter %s", key, baseDN, filter);
                }
            } catch (NamingException e) {
                LOGGER.warn("Error when getting LDAP attribute", e);
            }

        });
        return result;
    }
}
