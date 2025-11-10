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
import org.keycloak.models.RealmProvider;

/**
 * @author Luciano Di Leonardo
 * @email ldileona@redhat.com
 * @date 24 Apr 2023
 * @company Red Hat inc.
 * @role Architect
 */
public class SsoCustomEventListenerProvider implements EventListenerProvider {

    private static final Logger LOGGER = Logger.getLogger(SsoCustomEventListenerProvider.class.getName());

    private final KeycloakSession session;
    private final RealmProvider model;
    private final UserService userService;
    private final EventListenerTransaction transaction = new EventListenerTransaction(
            this::handleAdminEvent,
            this::handleClientEvent
    );

    public SsoCustomEventListenerProvider(KeycloakSession session, UserService userService) {
        this.session = session;
        this.model = session.realms();
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
                RealmModel realm = model.getRealm(event.getRealmId());
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

    private void handleAdminEvent(AdminEvent event, boolean includeRepresentation) {
        //Not useful
    }

    @Override
    public void close() {
        //Not useful
    }

}
