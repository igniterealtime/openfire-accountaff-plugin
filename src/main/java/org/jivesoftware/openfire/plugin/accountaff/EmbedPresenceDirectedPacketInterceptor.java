package org.jivesoftware.openfire.plugin.accountaff;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.openfire.roster.RosterItem;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import javax.annotation.Nonnull;

public class EmbedPresenceDirectedPacketInterceptor implements PacketInterceptor
{
    private static final Logger Log = LoggerFactory.getLogger(EmbedPresenceDirectedPacketInterceptor.class);

    public static final String NAMESPACE = "urn:xmpp:raa:0#embed-presence-directed";

    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException
    {
        if (processed) {
            // Stanzas cannot be modified after they've been processed.
            return;
        }

        if (!incoming || !(session instanceof LocalClientSession)) {
            // This functionality only applies to local users. Do not process any other stanzas.
            return;
        }

        if (!(packet instanceof Presence) || ((Presence) packet).getType() != null) {
            // This implementation only acts on directed presence stanzas.
            return;
        }

        // Protect against our own clients trying to spoof. The XEP dictates that the server MUST strip
        // client-originating <info/> elements.
        final boolean didRemoveSomething = packet.deleteExtension("info", Info.NAMESPACE);
        if (didRemoveSomething) {
            Log.info("Prevented RAA spoofing: Removed RAA 'info' extension from presence stanza of type {} sent by {} addressed to: {}", ((Presence) packet).getType(), packet.getFrom(), packet.getTo());
        }

        final JID originator = packet.getFrom();
        final JID recipient = packet.getTo();
        if (originator == null) {
            // This should not be possible, right? Be verbose if the unexpected does occur!
            Log.warn("Unable to process directed presence stanza, as the stanza has no 'from' attribute: {}", packet);
        } else if (recipient == null) {
            // This is a regular presence update. This will be pushed by the server to subscribed entities only. Safe to ignore.
            Log.trace("Not processing a regular presence update sent by '{}'.", originator);
        } else {
            final boolean isSubscribedToRecipient = isSubscribedToRecipient(originator.getNode(), recipient);
            if (isSubscribedToRecipient) {
                Log.trace("Skip adding info to directed presence stanza from user '{}' to user '{}' as the originator is subscribed to the presence of the recipient.", originator, recipient);
            } else {
                Log.trace("Attempting to add info to directed presence stanza from user '{}' to user '{}'", originator, recipient);
                final Info info = InfoDAO.lookup(originator);
                if (info != null) {
                    packet.getElement().add(info.asElement());
                }
            }
        }
    }

    public static boolean isSubscribedToRecipient(@Nonnull final String username, @Nonnull final JID recipient) {
        try {
            final Roster roster = XMPPServer.getInstance().getRosterManager().getRoster(username);
            final RosterItem.SubType subType = roster.getRosterItem(recipient).getSubStatus();
            return subType == RosterItem.SUB_TO || subType == RosterItem.SUB_BOTH;
        } catch (UserNotFoundException e) {
            return false;
        }
    }
}
