package com.redhat.sso.service;

import org.jboss.logging.Logger;

import javax.naming.CommunicationException;
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

public class LdapService {

    public static class LdapCtxCreationException extends NamingException {
    
        public LdapCtxCreationException(NamingException cause) {

            super("Error creating the LDAP context: " + cause.getMessage());
            initCause(cause);
            setRemainingName(cause.getRemainingName());
            setResolvedName(cause.getResolvedName());
            setResolvedObj(cause.getResolvedObj());
        }
    }

    private static final Logger LOGGER = Logger.getLogger(LdapService.class.getName());

    private final List<String> providerUrls;
    private final String securityPrincipal;
    private final String securityCredentials;

    LdapService(List<String> providerUrls, String securityPrincipal, String securityCredentials) {

        this.providerUrls = providerUrls;
        this.securityPrincipal = securityPrincipal;
        this.securityCredentials = securityCredentials;
    }

    private DirContext initContext() throws NamingException {

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrls.stream().reduce("", (a, b) -> a + " " + b));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.timeout", "10000"); // 10 secs
        env.put("com.sun.jndi.ldap.read.timeout", "10000"); // 10 secs
        
        return new InitialDirContext(env);
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

        DirContext ctx = createContext();

        String filter = String.format("(%s=%s)", uniqueAttribute, userName);
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> search;
        try {

            search = ctx.search(baseDN, filter, searchControls);
        } catch (CommunicationException e) {
            LOGGER.warn("LDAP Connection reset detect:" + e.getMessage() + ". Retrying...");

            closeContext(ctx); 

            ctx = createContext();
            
            search = ctx.search(baseDN, filter, searchControls);
        }
        finally {
            closeContext(ctx);
        }

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

    private void closeContext(DirContext ctx) {
        try {
            ctx.close();
        } catch (NamingException e) {
            LOGGER.warn("Error closing the LDAP context: " + e.getMessage(), e);
        }    
    }

    private DirContext createContext() throws LdapCtxCreationException {
        DirContext ctx;
        try {
            ctx = initContext();
            LOGGER.debug("LDAP Context initialized");
        } catch (NamingException e) {
            throw new LdapCtxCreationException(e);
        }
        return ctx;
    }
}
