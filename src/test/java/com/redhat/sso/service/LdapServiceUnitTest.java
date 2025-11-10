package com.redhat.sso.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Attr;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class LdapServiceUnitTest {

    LdapService service = new LdapService();

    @Test
    void testNoUserFound() throws NamingException {
        DirContext dirContext = mock(DirContext.class);
        NamingEnumeration<SearchResult> namingEnumeration = mock(NamingEnumeration.class);
        when(namingEnumeration.hasMoreElements()).thenReturn(false);

        when(dirContext.search(anyString(),anyString(),any(SearchControls.class))).thenReturn(namingEnumeration);

        Map<String,String> attributeMapping = new HashMap<>();
        IllegalArgumentException noRecordsFound = assertThrows(IllegalArgumentException.class, () -> {
            service.searchUserOnExternalLDAP(dirContext, "baseDN", "uniqueAttribute", "userName", attributeMapping);
        });

        assertThat(noRecordsFound.getMessage(), equalTo("Found 0 record(s) using baseDN baseDN and filter (uniqueAttribute=userName). Expected 1"));
    }

    @Test
    void testSearchingExistentAttributes() throws NamingException {
        DirContext dirContext = mock(DirContext.class);
        NamingEnumeration<SearchResult> namingEnumeration = new MyEnumeration();
        //when(namingEnumeration.hasMoreElements()).thenReturn(false);

        when(dirContext.search(anyString(),anyString(),any(SearchControls.class))).thenReturn(namingEnumeration);

        Map<String,String> attributeMapping = new HashMap<>();
        attributeMapping.put("ldap_attribute","transcoded_attribute");

        Map<String, String> stringStringMap = service.searchUserOnExternalLDAP(dirContext, "baseDN", "uniqueAttribute", "userName", attributeMapping);
        assertThat(stringStringMap.get("transcoded_attribute"),equalTo("ldap_attr_value"));

    }

    @Test
    void testSearchingInvalidAttribute() throws NamingException {
        DirContext dirContext = mock(DirContext.class);
        NamingEnumeration<SearchResult> namingEnumeration = new MyEnumeration();
        //when(namingEnumeration.hasMoreElements()).thenReturn(false);

        when(dirContext.search(anyString(),anyString(),any(SearchControls.class))).thenReturn(namingEnumeration);

        Map<String,String> attributeMapping = new HashMap<>();

        attributeMapping.put("ldap_attribute2","transcoded_attribute2");
        Map<String, String> stringStringMap = service.searchUserOnExternalLDAP(dirContext, "baseDN", "uniqueAttribute", "userName", attributeMapping);
        assertThat(stringStringMap.size(), equalTo(0));
        assertThat(stringStringMap.get("transcoded_attribute2"),is(nullValue()));
    }

    @Test
    void testSearchingAttributeWithException() throws NamingException {
        DirContext dirContext = mock(DirContext.class);
        NamingEnumeration<SearchResult> namingEnumeration = new MyEnumeration();
        //when(namingEnumeration.hasMoreElements()).thenReturn(false);

        when(dirContext.search(anyString(),anyString(),any(SearchControls.class))).thenReturn(namingEnumeration);

        Map<String,String> attributeMapping = new HashMap<>();

        attributeMapping.put("ldap_attribute_exception","transcoded_attribute");
        Map<String, String> stringStringMap = service.searchUserOnExternalLDAP(dirContext, "baseDN", "uniqueAttribute", "userName", attributeMapping);
        assertThat(stringStringMap.size(), equalTo(0));
        assertThat(stringStringMap.get("transcoded_attribute"),is(nullValue()));
    }

    @Test
    void testSearchingAttributeWhoseGetIsNull() throws NamingException {
        DirContext dirContext = mock(DirContext.class);
        NamingEnumeration<SearchResult> namingEnumeration = new MyEnumeration();
        //when(namingEnumeration.hasMoreElements()).thenReturn(false);

        when(dirContext.search(anyString(),anyString(),any(SearchControls.class))).thenReturn(namingEnumeration);

        Map<String,String> attributeMapping = new HashMap<>();

        attributeMapping.put("ldap_attribute_null","transcoded_attribute");
        Map<String, String> stringStringMap = service.searchUserOnExternalLDAP(dirContext, "baseDN", "uniqueAttribute", "userName", attributeMapping);
        assertThat(stringStringMap.size(), equalTo(0));
        assertThat(stringStringMap.get("transcoded_attribute"),is(nullValue()));
    }



    private static class MyEnumeration implements NamingEnumeration<SearchResult> {

        private boolean hasMore = true;
        private final SearchResult result =mock(SearchResult.class);

        public MyEnumeration() throws NamingException {
            Attributes attributes = mock(Attributes.class);
            Attribute attribute = mock(Attribute.class);
            Attribute attributeException = mock(Attribute.class);
            Attribute attributeNull = mock(Attribute.class);
            doAnswer(invocationOnMock -> {
                if (invocationOnMock.getArgument(0,String.class).equals("ldap_attribute")){
                    return attribute;
                }
                if (invocationOnMock.getArgument(0,String.class).equals("ldap_attribute_exception")){
                    return attributeException;
                }
                if (invocationOnMock.getArgument(0,String.class).equals("ldap_attribute_null")){
                    return attributeNull;
                }
                return  null;
            }).when(attributes).get(anyString());

            when(attribute.get()).thenReturn("ldap_attr_value");
            when(attributeNull.get()).thenReturn(null);
            when(attributeException.get()).thenThrow(new NamingException("Attribute not found"));
            when(result.getAttributes()).thenReturn(attributes);
        }

        @Override
        public SearchResult next() throws NamingException {
            if (hasMore){
                hasMore = false;
                return result;
            }
            return null;
        }

        @Override
        public boolean hasMore() throws NamingException {
            return hasMore;
        }

        @Override
        public void close() throws NamingException {

        }

        @Override
        public boolean hasMoreElements() {
            return hasMore;
        }

        @Override
        public SearchResult nextElement() {
            if (hasMore){
                hasMore = false;
                return result;
            }
            return null;
        }
    }

}