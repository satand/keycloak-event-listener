# How to configure LDAP in a local environment

## Prerequisites:

 - a recent version of podman (or docker)
 - ldapsearch to test queries (optional)

## Virtual network

Start new internal network

```shell
$ podman network create --ignore  myldapnet
```
## OpenLDAP primary and secondary server 

Run openldap (slapd) for **primary** (or **EXTERNAL**) directory:

```shell
$ podman run -it --replace  -p 3389:389 -p 3636:636 --net myldapnet --network-alias openldap-server --name openldap-server --env LDAP_ORGANISATION="My Company" --env LDAP_DOMAIN="ldap.example.com" --env LDAP_ADMIN_PASSWORD="password"  docker.io/osixia/openldap:latest
```

Run openldap (slapd) for **secondary** (or **INTERNAL**) directory:

```shell
$ podman run -it --replace  -p 4389:389 -p 4636:636 --net myldapnet --network-alias openldap-server-secondary --name openldap-server-secondary --env LDAP_ORGANISATION="My Company" --env LDAP_DOMAIN="ldap.secondary.example.com" --env LDAP_ADMIN_PASSWORD="password"  docker.io/osixia/openldap:latest
```

## Testing login with admin with ldapsearch

Search of the admin user on primary server: 

```shell
$ ldapsearch -x -H ldap://localhost:3389 -D "cn=admin,dc=ldap,dc=example,dc=com" -b "dc=ldap,dc=example,dc=com" -w password
```
or, if you prefer, use the container instead:

```shell
$ podman run -it --net myldapnet --entrypoint="/usr/bin/ldapsearch" docker.io/osixia/openldap:latest -x -H ldap://openldap-server:389 -D "cn=admin,dc=ldap,dc=example,dc=com" -b "dc=ldap,dc=example,dc=com" -w password
```


Search of the admin user on the secondary server:

```shell
$ ldapsearch -x -H ldap://localhost:4389 -D "cn=admin,dc=ldap,dc=secondary,dc=example,dc=com" -b "dc=ldap,dc=secondary,dc=example,dc=com" -w password
```

using the container:

```shell
$ podman run -it --net myldapnet --entrypoint="/usr/bin/ldapsearch" docker.io/osixia/openldap:latest -x -H ldap://openldap-server-secondary:389 -D "cn=admin,dc=ldap,dc=secondary,dc=example,dc=com" -b "dc=ldap,dc=secondary,dc=example,dc=com" -w password
```

## Testing with Web UI (phpLDAPadmin)

Run myphpldapadmin:

```shell
$ podman run -it --replace --name phpldapadmin -p 10080:80 -p 10443:443 --hostname phpldapadmin-service --net myldapnet --network-alias phpldapadmin-service --env PHPLDAPADMIN_LDAP_HOSTS="openldap-server"  docker.io/osixia/phpldapadmin:latest
```

Another instance for the secondary:

```shell
$ podman run -it --replace --name phpldapadmin-secondary -p 20080:80 -p 20443:443 --hostname phpldapadmin-service-secondary --net myldapnet --network-alias phpldapadmin-service-secondary --env PHPLDAPADMIN_LDAP_HOSTS="openldap-server-secondary"   docker.io/osixia/phpldapadmin:latest
```

phpLDAPadmin on https://localhost:10443 (for primary) 

login with:
 - username: **cn=admin,dc=ldap,dc=example,dc=com**
 - password: **password**

phpLDAPadmin on https://localhost:20443 (for secondary)

login with:
 - username: **cn=admin,dc=ldap,dc=secondary,dc=example,dc=com**
 - password: **password**


## Loading a more complex schema

In order to have a couple of users you can import the following LDAP schemas
LDIF of my whole LDAP tree for the primary forest.

Schema for the primary is located [here](external.ldif)
Schema for the secondary is located [here](internal.ldif)

To add the schema you can import the ldif file through the web UI of phpLDAPAdmin or by executing the following commands.

For primary domain:

```shell
$ ldapadd -c -x -H ldap://localhost:3389 -D "cn=admin,dc=ldap,dc=example,dc=com" -w password -f external.ldif
```

or using the existing container
```shell
$ podman cp external.ldif openldap-server:/tmp/external.ldif && podman exec -it openldap-server ldapadd -c -x -H ldap://localhost:389 -D 'cn=admin,dc=ldap,dc=example,dc=com' -w password -f /tmp/external.ldif
```

For secondary domain:

```shell
$ ldapadd -c -x -H ldap://localhost:4389 -D "cn=admin,dc=ldap,dc=secondary,dc=example,dc=com" -w password -f internal.ldif
```

or using the existing container
```shell
$ podman cp internal.ldif openldap-server-secondary:/tmp/internal.ldif && podman exec -it openldap-server-secondary ldapadd -c -x -H ldap://localhost:389 -D 'cn=admin,dc=ldap,dc=secondary,dc=example,dc=com' -w password -f /tmp/internal.ldif
```

In the current configuration we have the following users configured in both primary (external) and secondary (internal) directory:
 - croot 
 - mrossi
 - jdoe
 - lbird

The following ones are present only in the primary (external) directory:
 - jadmin
 - lsnidero
 - jstandard

The only difference between the schemas is that users in the primary (external) directory tree have the following custom attributes in addition:
 - title
 - employeeNumber

## Example login

Before to continue you have to regenerate the client secret: go to the RH SSO admin console and access to the client list; select the client named 'merge-domain-client' and in the tab Credentials click on the 'Regenerate Secret' button and copy the new client secret; execute the following command replacing the copied value:

```shell
export CLIENT_SECRET="<client_secret>"
```

Login with `Larry Bird`:

```shell
$ curl -d 'username=lbird' -v http://localhost:8080/auth/realms/multiple-ldap/protocol/openid-connect/token -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password' -d 'client_id=merge-domain-client' -d 'password=password' -d "client_secret=${CLIENT_SECRET}" | \
jq -r .access_token | \
cut -d '.' -f2 | \
sed 's/-/+/g; s/_/\//g' | \
awk '{ len = length % 4; if (len == 2) { print $0 "==" } else if (len == 3) { print $0 "=" } else { print $0 } }' | \
base64 -d | \
jq
```
or using python to decode Base64Url:
```shell
$ curl -d 'username=lbird' -v http://localhost:8080/auth/realms/multiple-ldap/protocol/openid-connect/token -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password' -d 'client_id=merge-domain-client' -d 'password=password' -d "client_secret=${CLIENT_SECRET}" | \
jq -r .access_token | \
cut -d '.' -f2 | \
python3 -c "import base64, sys; print(base64.urlsafe_b64decode(sys.stdin.read() + '==').decode())" | \
jq
```
or using perl to decode Base64Url:
```shell
$ curl -d 'username=lbird' -v http://localhost:8080/auth/realms/multiple-ldap/protocol/openid-connect/token -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password' -d 'client_id=merge-domain-client' -d 'password=password' -d "client_secret=${CLIENT_SECRET}" | \
jq -r .access_token | \
cut -d '.' -f2 | \
perl -MMIME::Base64=decode_base64url -ne 'print decode_base64url($_)' | \
jq
```

This user exists also in the **INTERNAL** directory where he lacks the `roomNumber` property. 
In the internal repository his name and surname (`givenName` and `sn` LDAP attributes) are in uppercase. In the **EXTERNAL** directory they appear in the correct case.  

The output is something like this:
```json
{
  "exp": 1743498858,
  "iat": 1743498558,
  "jti": "e2dd3728-0655-4225-9b8e-355031e7e674",
  "iss": "http://localhost:8080/auth/realms/multiple-ldap",
  "aud": "account",
  "sub": "1e83c0da-e3ab-4f01-81e5-51fb6c7ce075",
  "typ": "Bearer",
  "azp": "merge-domain-client",
  "session_state": "afd78594-9946-4f6d-83c1-a54d63be4e31",
  "acr": "1",
  "allowed-origins": [
    "*"
  ],
  "realm_access": {
    "roles": [
      "offline_access",
      "default-roles-multiple-ldap",
      "uma_authorization"
    ]
  },
  "resource_access": {
    "account": {
      "roles": [
        "manage-account",
        "manage-account-links",
        "view-profile"
      ]
    }
  },
  "scope": "profile email",
  "sid": "afd78594-9946-4f6d-83c1-a54d63be4e31",
  "titolo": "Basketball player",
  "email_verified": false,
  "numero": "5",
  "initials": "LB",
  "stanza": "33",
  "name": "lbird BIRD",
  "sn": "Bird",
  "preferred_username": "lbird",
  "given_name": "lbird",
  "userID": "1006",
  "family_name": "BIRD",
  "email": "lbird@example.com"
}
```
Note that the `family_name` token claim "comes" from the original **INTERNAL** directory `sn` attribute and is populated using a standard mapping of RH-SSO claims. 
The `sn` claim is created from the custom provider logic thus using the value written on the **EXTERNAL** LDAP directory. 

Login with `Johnny Standard`
```shell
$ curl -d 'username=jstandard' -v http://localhost:8080/auth/realms/multiple-ldap/protocol/openid-connect/token -H 'Content-Type: application/x-www-form-urlencoded' -d 'grant_type=password' -d 'client_id=merge-domain-client' -d 'password=password' -d "client_secret=${CLIENT_SECRET}" | \
jq -r .access_token | \
cut -d '.' -f2 | \
sed 's/-/+/g; s/_/\//g' | \
awk '{ len = length % 4; if (len == 2) { print $0 "==" } else if (len == 3) { print $0 "=" } else { print $0 } }' | \
base64 -d | \
jq
```

## Stop containers

```shell
$ podman stop --ignore openldap-server openldap-server-secondary phpldapadmin-service phpldapadmin-service-secondary
```

## Useful links

 - [How To Run OpenLDAP Server in Docker Containers | ComputingForGeeks](https://computingforgeeks.com/run-openldap-server-in-docker-containers/)
 - [OpenLDAP and Docker compose | OpenLDAP](https://medium.com/@devripper133127/setting-up-openldap-and-phpldapadmin-with-docker-compose-cf2336590989)

## Bonus tip

How do I know my user's encrypted password is ok?

```shell
$ podman run -it --entrypoint="/usr/sbin/slappasswd"  docker.io/osixia/openldap:latest -h {MD5} -s "password"
```

The output will be `{MD5}X03MO1qnZdYdgyfeuILPmQ==`

## LDAP example filters

primary == external

Ldap filter:
```
(initials=RIE*)
```

secondary == internal

Ldap filter: 
```
(!(initials=RIE*))
```