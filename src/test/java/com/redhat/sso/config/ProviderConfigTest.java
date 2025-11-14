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
import java.util.Collections;

class ProviderConfigTest {


    @Test
    void testAllArgsConstructor() {
        ProviderConfig configuration = new ProviderConfig("true", "true", 
            "ldap://localhost:3389,ldap://localhost:4389", "cn=admin,dc=ldap,dc=example,dc=com", 
            "password", "ou=users,dc=ldap,dc=secondary,dc=example,dc=com", "employeeNumber=numero,title=titolo", "cn");
        assertThat(configuration.isEventListenerEnabled(), equalTo(true));
        assertThat(configuration.isMapperEnabled(), equalTo(true));
        assertThat(configuration.getProviderUrls(), equalTo(Arrays.asList("ldap://localhost:3389", "ldap://localhost:4389")));
        assertThat(configuration.getSecurityPrincipal(), equalTo("cn=admin,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getSecurityCredentials(), equalTo("password"));
        assertThat(configuration.getUsersDN(), equalTo("ou=users,dc=ldap,dc=secondary,dc=example,dc=com"));
        assertThat(configuration.getExternalAttributes().size(), is(2));
        assertThat(configuration.getExternalAttributes().get("employeeNumber"), equalTo("numero"));
        assertThat(configuration.getExternalAttributes().get("title"), equalTo("titolo"));
        assertThat(configuration.getExternalUsernameFilter(), equalTo("cn"));
    }

    @Test
    void testAllArgsConstructorWithEventListenerDisabledAndMapperEnabled() {
        ProviderConfig configuration = new ProviderConfig("false", "true", 
            "ldap://localhost:3389,ldap://localhost:4389", "cn=admin,dc=ldap,dc=example,dc=com", 
            "password", "ou=users,dc=ldap,dc=secondary,dc=example,dc=com", "employeeNumber=numero,title=titolo", "cn");
        assertThat(configuration.isEventListenerEnabled(), equalTo(false));
        assertThat(configuration.isMapperEnabled(), equalTo(true));
        assertThat(configuration.getProviderUrls(), equalTo(Arrays.asList("ldap://localhost:3389", "ldap://localhost:4389")));
        assertThat(configuration.getSecurityPrincipal(), equalTo("cn=admin,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getSecurityCredentials(), equalTo("password"));
        assertThat(configuration.getUsersDN(), equalTo("ou=users,dc=ldap,dc=secondary,dc=example,dc=com"));
        assertThat(configuration.getExternalAttributes().size(), is(2));
        assertThat(configuration.getExternalAttributes().get("employeeNumber"), equalTo("numero"));
        assertThat(configuration.getExternalAttributes().get("title"), equalTo("titolo"));
        assertThat(configuration.getExternalUsernameFilter(), equalTo("cn"));
    }

    @Test
    void testAllArgsConstructorWithEventListenerEnabledAndMapperDisabled() {
        ProviderConfig configuration = new ProviderConfig("true", "false", 
            "ldap://localhost:3389,ldap://localhost:4389", "cn=admin,dc=ldap,dc=example,dc=com", 
            "password", "ou=users,dc=ldap,dc=secondary,dc=example,dc=com", "employeeNumber=numero,title=titolo", "cn");
        assertThat(configuration.isEventListenerEnabled(), equalTo(true));
        assertThat(configuration.isMapperEnabled(), equalTo(false));
        assertThat(configuration.getProviderUrls(), equalTo(Arrays.asList("ldap://localhost:3389", "ldap://localhost:4389")));
        assertThat(configuration.getSecurityPrincipal(), equalTo("cn=admin,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getSecurityCredentials(), equalTo("password"));
        assertThat(configuration.getUsersDN(), equalTo("ou=users,dc=ldap,dc=secondary,dc=example,dc=com"));
        assertThat(configuration.getExternalAttributes().size(), is(2));
        assertThat(configuration.getExternalAttributes().get("employeeNumber"), equalTo("numero"));
        assertThat(configuration.getExternalAttributes().get("title"), equalTo("titolo"));
        assertThat(configuration.getExternalUsernameFilter(), equalTo("cn"));
    }

    @Test
    void testAllArgsConstructorWithEventListenerDisabledAndMapperDisabled() {
        ProviderConfig configuration = new ProviderConfig("false", "false", 
            "ldap://localhost:3389,ldap://localhost:4389", "cn=admin,dc=ldap,dc=example,dc=com", 
            "password", "ou=users,dc=ldap,dc=secondary,dc=example,dc=com", "employeeNumber=numero,title=titolo", "cn");
        assertThat(configuration.isEventListenerEnabled(), equalTo(false));
        assertThat(configuration.isMapperEnabled(), equalTo(false));
        assertThat(configuration.getProviderUrls(), equalTo(Collections.emptyList()));
        assertThat(configuration.getSecurityPrincipal(), equalTo(null));
        assertThat(configuration.getSecurityCredentials(), equalTo(null));
        assertThat(configuration.getUsersDN(), equalTo(null));
        assertThat(configuration.getExternalAttributes(), equalTo(Collections.emptyMap()));
        assertThat(configuration.getExternalUsernameFilter(), equalTo(null));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389,ldap://localhost:4389"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
            @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
    })
    @Test
    void testEnvConstructorWithEventListenerEnabledAndMapperEnabledNotDefined() {
        ProviderConfig configuration = new ProviderConfig();
        assertThat(configuration.isEventListenerEnabled(), equalTo(true));
        assertThat(configuration.isMapperEnabled(), equalTo(true));
        assertThat(configuration.getProviderUrls(), equalTo(Arrays.asList("ldap://localhost:3389", "ldap://localhost:4389")));
        assertThat(configuration.getSecurityPrincipal(), equalTo("cn=admin,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getSecurityCredentials(), equalTo("password"));
        assertThat(configuration.getUsersDN(), equalTo("ou=users,dc=ldap,dc=example,dc=com"));
        assertThat(configuration.getExternalAttributes().size(), is(2));
        assertThat(configuration.getExternalAttributes().get("employeeNumber"), equalTo("numero"));
        assertThat(configuration.getExternalAttributes().get("title"), equalTo("titolo"));
        assertThat(configuration.getExternalUsernameFilter(), equalTo("cn"));
    }

    @SetEnvironmentVariable.SetEnvironmentVariables({
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_EVENT_LISTENER_ENABLED, value = "TRUE"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_MAPPER_ENABLED, value = "TRUE"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS, value = "ldap://localhost:3389,ldap://localhost:4389"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_PRINCIPAL, value = "cn=admin,dc=ldap,dc=example,dc=com"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_SECURITY_CREDENTIALS, value = "password"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERS_DN, value = "ou=users,dc=ldap,dc=example,dc=com"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_ATTRIBUTE_MAP, value = "employeeNumber=numero,title=titolo"),
        @SetEnvironmentVariable(key = ProviderConfig.EXTERNAL_LDAP_USERNAME_FILTER, value = "cn")
})
@Test
void testEnvConstructorWithEventListenerEnabledAndMapperEnabledDefined() {
    ProviderConfig configuration = new ProviderConfig();
    assertThat(configuration.isEventListenerEnabled(), equalTo(true));
    assertThat(configuration.isMapperEnabled(), equalTo(true));
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

}