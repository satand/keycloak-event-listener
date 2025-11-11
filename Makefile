# If It's necessary define your JAVA_HOME
#export JAVA_HOME=<YOUR_JAVA_HOME>

export SSO_PATH=$(CURDIR)/RHSSO/rh-sso-7.6

compile:
	./mvnw clean package

prepare-sso: compile
	cp target/sso-event-listener-provider.jar ${SSO_PATH}/standalone/deployments

run-sso: prepare-sso compile
	export EXTERNAL_LDAP_FEDERATION_PROVIDER_URL=ldap://localhost:3389 && \
    export EXTERNAL_LDAP_SECURITY_PRINCIPAL=cn=admin,dc=ldap,dc=example,dc=com && \
    export EXTERNAL_LDAP_SECURITY_CREDENTIALS=password && \
    export EXTERNAL_LDAP_USERS_DN=ou=users,dc=ldap,dc=example,dc=com && \
    export EXTERNAL_LDAP_ATTRIBUTE_MAP=title=titolo,non_existent_attribute=non_existent_attribute,employeenumber=numero,uidnumber=userID,roomNumber=stanza,sn=sn && \
    export EXTERNAL_LDAP_USERNAME_FILTER=cn && \
	${SSO_PATH}/bin/standalone.sh -b 0.0.0.0
    # -Djboss.socket.binding.port-offset=1000