package com.redhat.sso.service;

import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.ApacheDSTestExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.redhat.sso.service.LdapService.LdapCtxCreationException;

import javax.naming.NamingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


@ExtendWith(ApacheDSTestExtension.class)
@CreateDS(name = "TestSecondaryDS", partitions = {@CreatePartition(name = "TestPartition", suffix = "dc=myorg,dc=com")}, allowAnonAccess = false)
@ApplyLdifFiles({"users.ldif"})
class LdapServiceTest extends AbstractLdapTestUnit {

    private LdapService service;

    @AfterEach
    void clean() {

        LdapService.close(service);
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390),
                                    @CreateTransport(protocol = "LDAP", address = "localhost", port = 21390)})
    @Test
    void testLdapContextWhenAllTheServersAreAvailable() {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
    @Test
    void testLdapContextWhenOnlyTheFirstServerIsAvailable() {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 21390)})
    @Test
    void testLdapContextWhenOnlyTheSecondServerIsAvailable() {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");
    }

    @Test
    void testLdapContextWhenNoServerIsAvailable() {

        Assertions.assertThrows(LdapCtxCreationException.class, () -> {
            service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");
        });
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
    @Test
    void testUserFoundWithUidFromRootDC() throws NamingException {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");

        Map<String, String> userAttributes = service.searchUserOnExternalLDAP(
            "dc=myorg,dc=com", "uid", "ldaptest1", attributeMapping);
        
        assertThat(userAttributes.size(), equalTo(2));
        assertThat(userAttributes.get("titolo"), equalTo("Worker"));
        assertThat(userAttributes.get("numero"), equalTo("42"));
        assertThat(userAttributes.get("title"), nullValue());
        assertThat(userAttributes.get("employeenumber"), nullValue());
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
    @Test
    void testUserFoundWithCommonName() throws NamingException {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");

        Map<String, String> userAttributes = service.searchUserOnExternalLDAP(
            "ou=Users,dc=myorg,dc=com", "cn", "Test1 Ldap", attributeMapping);

        assertThat(userAttributes.size(), equalTo(2));
        assertThat(userAttributes.get("titolo"), equalTo("Worker"));
        assertThat(userAttributes.get("numero"), equalTo("42"));
        assertThat(userAttributes.get("title"), nullValue());
        assertThat(userAttributes.get("employeenumber"), nullValue());
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
    @Test
    void testExceptionUserNotFound() throws NamingException {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");
        attributeMapping.put("fiscalcode", "codice_fiscale");

        IllegalArgumentException noRecordFound = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.searchUserOnExternalLDAP("ou=Users,dc=myorg,dc=com", "uid", "User Not Existent", attributeMapping);
        });

        assertThat(noRecordFound.getMessage(), equalTo("Found 0 record(s) using baseDN ou=Users,dc=myorg,dc=com and filter (uid=User Not Existent). Expected 1"));
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
    @Test
    void testExceptionMultipleUsersFound() throws NamingException {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");
        attributeMapping.put("fiscalcode", "codice_fiscale");

        IllegalArgumentException noRecordFound = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.searchUserOnExternalLDAP("ou=Users,dc=myorg,dc=com", "sn", "Ldap", attributeMapping);
        });

        assertThat(noRecordFound.getMessage(), equalTo("Found 2 record(s) using baseDN ou=Users,dc=myorg,dc=com and filter (sn=Ldap). Expected 1"));
    }

    @CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
    @Test
    void testNoExceptionOnAttributeNotFound() throws NamingException {
        service = LdapService.getLdapService(Arrays.asList("ldap://localhost:11390", "ldap://localhost:21390"), 
            "uid=admin,ou=system", "secret");

        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("non_existent_attribute", "attributo_non_presente");
        attributeMapping.put("employeenumber", "numero");

        Map<String, String> userAttributes = service.searchUserOnExternalLDAP(
            "ou=Users,dc=myorg,dc=com", "cn", "Test1 Ldap", attributeMapping);

        assertThat(userAttributes.size(), equalTo(2));
        assertThat(userAttributes.get("titolo"), equalTo("Worker"));
        assertThat(userAttributes.get("numero"), equalTo("42"));
        assertThat(userAttributes.get("title"), nullValue());
        assertThat(userAttributes.get("employeenumber"), nullValue());
        assertThat(userAttributes.get("attributo_non_presente"), nullValue());
        assertThat(userAttributes.get("non_existent_attribute"), nullValue());
    }

}