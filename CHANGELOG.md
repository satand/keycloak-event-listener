# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.0.2] - 28/02/2025

This version addresses the problem of the generation of the access token using user attributes.
The problem we faced was the following:
1. the user requests the access token
2. the user performs login towards the correct LDAP federation 
3. RH-SSO generates the access token with claims containing actual user attributes
4. The custom provider intercepts the LOGIN event and updates user attributes

In this scenario the access token is generated too early respect our use case.

The change we made intercepts the access token generation adding at that level the custom claims.
In order to do that we need to configure in the client the custom protocol mapper **Multiple LDAP Claim Mapper**.

### Added

Added a custom OIDC protocol mapper in order to intercept the generation of the access token. 
Added a new service [UserService](src/main/java/com/redhat/sso/service/UserService.java) to handle better the 
testability of the application. 

## [1.0.1] - 26/02/2025

### Added

Added the `EXTERNAL_LDAP_USERNAME_FILTER` environment variable. This is used to map the filter on username.
Example values can be `cn` or `samaccountname`


### Changed

The env variable `EXTERNAL_LDAP_ATTRIBUTE_SET` is now called  `EXTERNAL_LDAP_ATTRIBUTE_MAP` and performs mapping on 
discovered values.
The format for this variable is something like `title=titolo,employeenumber=numero`

## [1.0.0] - 24/02/2025 

First version of the custom provider. This version works by executing an LDAP query on a directory configured with the 
following environment variables:

 - EXTERNAL_LDAP_FEDERATION_PROVIDER_URL, url of the directory (i.e. `ldap://localhost:3389`)
 - EXTERNAL_LDAP_SECURITY_PRINCIPAL, bind DN for searching on the directory (i.e. `cn=admin,dc=ldap,dc=example,dc=com`)
 - EXTERNAL_LDAP_SECURITY_CREDENTIALS, password for the bind DN 
 - EXTERNAL_LDAP_USERS_DN base DN for searching users (using CN as filter) (i.e. `ou=users,dc=ldap,dc=example,dc=com`)
 - EXTERNAL_LDAP_SEARCH_FILTER not used 
 - EXTERNAL_LDAP_ATTRIBUTE_SET set of LDAP attributes we need to add as user attributes (i.e. `title,employeenumber`)