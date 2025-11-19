# If It's necessary define your JAVA_HOME
#export JAVA_HOME=<YOUR_JAVA_HOME>

export SSO_PATH=$(CURDIR)/RHSSO/rh-sso-7.6

compile:
	./mvnw clean package

prepare-sso: compile
	cp target/sso-event-listener-provider.jar ${SSO_PATH}/standalone/deployments

run-sso: prepare-sso compile
	export EXTERNAL_LDAP_FEDERATION_EVENT_LISTENER_ENABLED="FALSE" && \
    export EXTERNAL_LDAP_FEDERATION_MAPPER_ENABLED="TRUE" && \
    export EXTERNAL_LDAP_FEDERATION_PROVIDER_URLS="ldap://localhost:2389,ldap://localhost:3389" && \
    export EXTERNAL_LDAP_SECURITY_PRINCIPAL=cn=admin,dc=ldap,dc=external,dc=example,dc=com && \
    export EXTERNAL_LDAP_SECURITY_CREDENTIALS=password && \
    export EXTERNAL_LDAP_USERS_DN=ou=users,dc=ldap,dc=external,dc=example,dc=com && \
    export EXTERNAL_LDAP_ATTRIBUTE_MAP=title=titolo,non_existent_attribute=non_existent_attribute,employeenumber=numero,uidnumber=userID,roomNumber=stanza,sn=sn && \
    export EXTERNAL_LDAP_USERNAME_FILTER=cn && \
	${SSO_PATH}/bin/standalone.sh -b 0.0.0.0

    ## Debug LDAP Connection Pool
    # -Dcom.sun.jndi.ldap.connect.pool.debug=all 
    ## Set specific LDAP Connection Pool maxsize (the default value is 1000) and timeout (the default value is 300000)
    # -Dcom.sun.jndi.ldap.connect.pool.maxsize=100 -Dcom.sun.jndi.ldap.connect.pool.timeout=60000
    ## Set JBOSS binding port offset 
    # -Djboss.socket.binding.port-offset=1000