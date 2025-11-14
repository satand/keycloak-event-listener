package com.redhat.sso.providers;

import com.redhat.sso.service.UserService;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class SsoCustomEventListenerProvider implements EventListenerProvider {

    private static final Logger LOGGER = Logger.getLogger(SsoCustomEventListenerProvider.class.getName());

    private final KeycloakSession session;
    private final UserService userService;
    private final EventListenerTransaction transaction = new EventListenerTransaction(
            null,
            this::handleClientEvent
    );

    public SsoCustomEventListenerProvider(KeycloakSession session, UserService userService) {
        this.session = session;
        this.userService = userService;
        // enlistPrepare -> if our transaction fails than the user is NOT verified
        // enlist -> if our transaction fails than the user is still verified
        // enlistAfterCompletion -> if our transaction fails our user is still verified
        this.session.getTransactionManager().enlistPrepare(this.transaction);
    }


    @Override
    public void onEvent(Event event) {
        transaction.addEvent(event);
    }

    void handleClientEvent(Event event) {
        long start = System.currentTimeMillis();
        try {
            if (EventType.LOGIN.equals(event.getType()) || EventType.IMPERSONATE.equals(event.getType())) {
                LOGGER.infof("Handling event with Type %s for userId %s", event.getType().name(), event.getUserId());
                RealmModel realm = session.realms().getRealm(event.getRealmId());
                userService.updateUser(realm, session, event.getUserId());
            }
        } catch (Exception ex) {
            LOGGER.error("Exception caught:", ex);
        } finally {
            LOGGER.infof("Ends in: %s ms.", (System.currentTimeMillis() - start));
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        transaction.addAdminEvent(adminEvent, includeRepresentation);
    }

    @Override
    public void close() {
        //Not useful
    }

}
