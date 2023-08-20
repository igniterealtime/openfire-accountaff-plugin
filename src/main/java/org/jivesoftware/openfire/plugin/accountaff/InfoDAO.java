package org.jivesoftware.openfire.plugin.accountaff;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

public class InfoDAO
{
    private static final Logger Log = LoggerFactory.getLogger(InfoDAO.class);

    public static Info lookup(@Nonnull final JID account)
    {
        Log.trace("Lookup account info of {}", account);
        if (!XMPPServer.getInstance().isLocal(account)) {
            Log.debug("Unable to lookup account info of {} - it is not an account on this server.", account);
            return null;
        }

        final String username = account.getNode();
        final Collection<ClientSession> sessions = SessionManager.getInstance().getSessions(username);
        final Optional<ClientSession> anonymousSession = sessions.stream()
            .filter(ClientSession::isAnonymousUser)
            .findAny();

        if (anonymousSession.isPresent()) {
            return new Info(Affiliation.anonymous, anonymousSession.get().getCreationDate().toInstant(), null);
        }

        try {
            final User registeredUser = UserManager.getInstance().getUser(username);
            final boolean isAdmin = AdminManager.getInstance().isUserAdmin(username, true);
            if (isAdmin) {
                return new Info(Affiliation.admin, registeredUser.getCreationDate().toInstant(), null);
            } else {
                final String raaAffPropertyValue = registeredUser.getProperties().get("RAA Affiliation");
                Affiliation affiliation = Affiliation.registered;
                if (raaAffPropertyValue != null) {
                    try {
                        final Affiliation affiliationProperty = Affiliation.valueOf(raaAffPropertyValue);
                        if (affiliationProperty == Affiliation.anonymous || affiliationProperty == Affiliation.admin) {
                            Log.warn("User '{}' has an extended property 'RAA Affiliation' set to the value of '{}' which is a value that cannot be manually configured. This property is being ignored.", username, raaAffPropertyValue);
                        } else {
                            affiliation = affiliationProperty;
                        }
                    } catch (IllegalArgumentException e) {
                        Log.warn("User '{}' has an extended property 'RAA Affiliation' but it does not match one of the recognized affiliation values. Offending value: {}", username, raaAffPropertyValue, e);
                    }
                }
                return new Info(affiliation, registeredUser.getCreationDate().toInstant(), null);
            }
        } catch (UserNotFoundException e) {
            return null;
        }
    }
}
