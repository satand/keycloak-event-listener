package com.redhat.sso.config;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderConfig {

    static final String EXTERNAL_LDAP_FEDERATION_PROVIDER_URL = "EXTERNAL_LDAP_FEDERATION_PROVIDER_URL";
    static final String EXTERNAL_LDAP_SECURITY_PRINCIPAL = "EXTERNAL_LDAP_SECURITY_PRINCIPAL";
    static final String EXTERNAL_LDAP_SECURITY_CREDENTIALS = "EXTERNAL_LDAP_SECURITY_CREDENTIALS";
    static final String EXTERNAL_LDAP_USERS_DN = "EXTERNAL_LDAP_USERS_DN";
    static final String EXTERNAL_LDAP_ATTRIBUTE_MAP = "EXTERNAL_LDAP_ATTRIBUTE_MAP";
    static final String EXTERNAL_LDAP_USERNAME_FILTER = "EXTERNAL_LDAP_USERNAME_FILTER";

    private static final String ENV_NOT_PRESENT_ERROR = "The environment variable %s is mandatory but is not present";

    private final String providerUrl;
    private final String securityPrincipal;
    private final String securityCredentials;
    private final String usersDN;
    private final Map<String, String> externalAttributes;
    private final String externalUsernameFilter;

    public ProviderConfig() {
        this(System.getenv(EXTERNAL_LDAP_FEDERATION_PROVIDER_URL),
                System.getenv(EXTERNAL_LDAP_SECURITY_PRINCIPAL),
                System.getenv(EXTERNAL_LDAP_SECURITY_CREDENTIALS),
                System.getenv(EXTERNAL_LDAP_USERS_DN),
                System.getenv(EXTERNAL_LDAP_ATTRIBUTE_MAP),
                System.getenv(EXTERNAL_LDAP_USERNAME_FILTER)
        );
    }

    public ProviderConfig(String providerUrl, String securityPrincipal, String securityCredentials, String usersDN, String externalAttributesMap, String externalUsernameFilter) {

        this.providerUrl = Optional.ofNullable(providerUrl).orElseThrow(() -> new IllegalArgumentException(String.format(ENV_NOT_PRESENT_ERROR, EXTERNAL_LDAP_FEDERATION_PROVIDER_URL)));
        this.securityPrincipal = Optional.ofNullable(securityPrincipal).orElseThrow(() -> new IllegalArgumentException(String.format(ENV_NOT_PRESENT_ERROR, EXTERNAL_LDAP_SECURITY_PRINCIPAL)));
        this.securityCredentials = Optional.ofNullable(securityCredentials).orElseThrow(() -> new IllegalArgumentException(String.format(ENV_NOT_PRESENT_ERROR, EXTERNAL_LDAP_SECURITY_CREDENTIALS)));
        this.usersDN = Optional.ofNullable(usersDN).orElseThrow(() -> new IllegalArgumentException(String.format(ENV_NOT_PRESENT_ERROR, EXTERNAL_LDAP_USERS_DN)));
        this.externalAttributes = Arrays.stream(
                        Optional.ofNullable(externalAttributesMap).orElseThrow(() -> new IllegalArgumentException(String.format(ENV_NOT_PRESENT_ERROR, EXTERNAL_LDAP_ATTRIBUTE_MAP)))
                                .split(","))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(k -> k[0], v -> v[1]));
        this.externalUsernameFilter = Optional.ofNullable(externalUsernameFilter).orElseThrow(() -> new IllegalArgumentException(String.format(ENV_NOT_PRESENT_ERROR, EXTERNAL_LDAP_USERNAME_FILTER)));

    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    public String getSecurityCredentials() {
        return securityCredentials;
    }

    public String getUsersDN() {
        return usersDN;
    }

    public Map<String, String> getExternalAttributes() {
        return externalAttributes;
    }

    public String getExternalUsernameFilter() {
        return externalUsernameFilter;
    }
}
