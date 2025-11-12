package com.redhat.sso.service;

import org.jboss.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(LdapService.class.getName());

    public DirContext initContext(List<String> providerUrls, String securityPrincipal, String securityCredentials) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrls.stream().reduce("", (a, b) -> a + " " + b));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
        // env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.timeout", "5000"); // 5 sec

        return new InitialDirContext(env);
    }

    /**
     * Search User directly with baseDN and (uniqueAttribute=userName) as filter
     *
     * @param ctx              LDAP context
     * @param baseDN           baseDN for the search
     * @param uniqueAttribute  attribute used on LDAP to filter user (usually cn or samaccountname)
     * @param userName         SSO username
     * @param attributeMapping map of attributes names to retrieve and how to map them to user
     * @return Map of key, value attributes found
     * @throws NamingException when the search went wrong
     */
    public Map<String, String> searchUserOnExternalLDAP(DirContext ctx, String baseDN, String uniqueAttribute, String userName, Map<String, String> attributeMapping) throws NamingException {
        String filter = String.format("(%s=%s)", uniqueAttribute, userName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> search = ctx.search(baseDN, filter, searchControls);
        ArrayList<SearchResult> list = Collections.list(search);

        if (list.size() != 1) {
            throw new IllegalArgumentException(String.format("Found %s record(s) using baseDN %s and filter %s. Expected 1", list.size(), baseDN, filter));
        }
        SearchResult userFound = list.stream().findFirst().orElseThrow(IllegalArgumentException::new);

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
