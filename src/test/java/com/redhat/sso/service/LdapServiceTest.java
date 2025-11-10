package com.redhat.sso.service;

import org.apache.directory.server.annotations.CreateLdapServer;
import org.apache.directory.server.annotations.CreateTransport;
import org.apache.directory.server.core.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.annotations.CreateDS;
import org.apache.directory.server.core.annotations.CreatePartition;
import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
import org.apache.directory.server.core.integ.ApacheDSTestExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


@ExtendWith(ApacheDSTestExtension.class)
@CreateLdapServer(transports = {@CreateTransport(protocol = "LDAP", address = "localhost", port = 11390)})
@CreateDS(name = "TestSecondaryDS", partitions = {@CreatePartition(name = "TestPartition", suffix = "dc=myorg,dc=com")}, allowAnonAccess = false)
@ApplyLdifFiles({"users.ldif"})
class LdapServiceTest extends AbstractLdapTestUnit {

    LdapService service = new LdapService();

    @Test
    void testUserExists() throws Exception {
        DirContext dirContext = service.initContext("ldap://localhost:11390", "uid=admin,ou=system", "secret");

        assertThat(dirContext, notNullValue());

    }

    @Test
    void testUserFoundWithUidFromRootDC() throws NamingException {
        DirContext dirContext = service.initContext("ldap://localhost:11390", "uid=admin,ou=system", "secret");
        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");
        Map<String, String> userAttributes = service.searchUserOnExternalLDAP(dirContext, "dc=myorg,dc=com", "uid", "ldaptest1", attributeMapping);
        assertThat(userAttributes.size(), equalTo(2));
        assertThat(userAttributes.get("titolo"), equalTo("Worker"));
        assertThat(userAttributes.get("numero"), equalTo("42"));
        assertThat(userAttributes.get("title"), nullValue());
        assertThat(userAttributes.get("employeenumber"), nullValue());
    }

    @Test
    void testUserFoundWithCommonName() throws NamingException {
        DirContext dirContext = service.initContext("ldap://localhost:11390", "uid=admin,ou=system", "secret");
        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");

        Map<String, String> userAttributes = service.searchUserOnExternalLDAP(dirContext, "ou=Users,dc=myorg,dc=com", "cn", "Test1 Ldap", attributeMapping);
        assertThat(userAttributes.size(), equalTo(2));
        assertThat(userAttributes.get("titolo"), equalTo("Worker"));
        assertThat(userAttributes.get("numero"), equalTo("42"));
        assertThat(userAttributes.get("title"), nullValue());
        assertThat(userAttributes.get("employeenumber"), nullValue());
    }

    @Test
    void testExceptionUserNotfound() throws NamingException {
        DirContext dirContext = service.initContext("ldap://localhost:11390", "uid=admin,ou=system", "secret");
        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("employeenumber", "numero");
        IllegalArgumentException noRecordFound = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.searchUserOnExternalLDAP(dirContext, "ou=Users,dc=myorg,dc=com", "uid", "User Not Existent", attributeMapping);
        });
        assertThat(noRecordFound.getMessage(), equalTo("Found 0 record(s) using baseDN ou=Users,dc=myorg,dc=com and filter (uid=User Not Existent). Expected 1"));
    }

    @Test
    void testNoMoreExceptionOnAttributeNotfound() throws NamingException {
        DirContext dirContext = service.initContext("ldap://localhost:11390", "uid=admin,ou=system", "secret");
        Map<String, String> attributeMapping = new HashMap<>();
        attributeMapping.put("title", "titolo");
        attributeMapping.put("non_existent_attribute", "non_existent_attribute");
        attributeMapping.put("employeenumber", "numero");


        Map<String, String> userAttributes = service.searchUserOnExternalLDAP(dirContext, "ou=Users,dc=myorg,dc=com", "cn", "Test1 Ldap", attributeMapping);

        assertThat(userAttributes.size(), equalTo(2));
        assertThat(userAttributes.get("titolo"), equalTo("Worker"));
        assertThat(userAttributes.get("numero"), equalTo("42"));
        assertThat(userAttributes.get("title"), nullValue());
        assertThat(userAttributes.get("employeenumber"), nullValue());

    }

}