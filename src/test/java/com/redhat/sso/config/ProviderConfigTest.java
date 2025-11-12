package com.redhat.sso.config;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

class ProviderConfigTest {

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389,ldap://localhost:4389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
    })
    @Test
    void testEnvConstructor() {
        ProviderConfig configuration = new ProviderConfig();
        assertThat(configuration.getProviderUrls(), equalTo(Arrays.asList("ldap://localhost:3389", "ldap://localhost:4389")));
        assertThat(configuration.getSecurityPrincipal(), equalTo("cn=admin,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getSecurityCredentials(), equalTo("password"));
        assertThat(configuration.getUsersDN(), equalTo("ou=users,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getExternalAttributes().size(), is(2));
        assertThat(configuration.getExternalAttributes().get("employeeNumber"), equalTo("numero"));
        assertThat(configuration.getExternalAttributes().get("title"), equalTo("titolo"));
        assertThat(configuration.getExternalUsernameFilter(), equalTo("cn"));
    }

    @Test
    void testExceptionOnConstuctionCausedByMissingEnv() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), allOf(containsString("The environment variable"), containsString(" is mandatory but is not present")));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
    })
    @Test
    void testExceptionOnMissingProviderUrlEnv() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), equalTo("The environment variable EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS is mandatory but is not present"));
    }


    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
    })
    @Test
    void testExceptionOnMissingSecurityPrincipalEnv() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), equalTo("The environment variable EXTERNAL_LDAP_SECURITY_PRINCIPAL is mandatory but is not present"));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
    })
    @Test
    void testExceptionOnMissingSecurityCredentialsEnv() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), equalTo("The environment variable EXTERNAL_LDAP_SECURITY_CREDENTIALS is mandatory but is not present"));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")

    })
    @Test
    void testExceptionOnMissingUsersDNEnv() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), equalTo("The environment variable EXTERNAL_LDAP_USERS_DN is mandatory but is not present"));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
    })
    @Test
    void testExceptionOnMissingAttributeMap() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), equalTo("The environment variable EXTERNAL_LDAP_ATTRIBUTE_MAP is mandatory but is not present"));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo")
    })
    @Test
    void testExceptionOnMissingUsernameFilter() {
        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, ProviderConfig::new);
        assertThat(illegalArgumentException.getMessage(), equalTo("The environment variable EXTERNAL_LDAP_USERNAME_FILTER is mandatory but is not present"));
    }


    @Test
    void testAllArgsConstructor() {
        ProviderConfig configuration = new ProviderConfig("ldap://localhost:3389,ldap://localhost:4389", "cn=admin,dc=ldap,dc=example,dc=com", "password", "ou=users,dc=ldap,dc=secondary,dc=example,dc=com", "employeeNumber=numero,title=titolo", "cn");
        assertThat(configuration.getProviderUrls(), equalTo(Arrays.asList("ldap://localhost:3389", "ldap://localhost:4389")));
        assertThat(configuration.getSecurityPrincipal(), equalTo("cn=admin,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getSecurityCredentials(), equalTo("password"));
        assertThat(configuration.getExternalAttributes().size(), is(2));
        assertThat(configuration.getExternalAttributes().get("employeeNumber"), equalTo("numero"));
        assertThat(configuration.getExternalAttributes().get("title"), equalTo("titolo"));
        assertThat(configuration.getExternalUsernameFilter(), equalTo("cn"));
    }

}