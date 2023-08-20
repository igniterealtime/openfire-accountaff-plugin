package org.jivesoftware.openfire.plugin.accountaff;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class EmbedPresenceSubPacketInterceptor implements PacketInterceptor
{
    private static final Logger Log = LoggerFactory.getLogger(EmbedPresenceSubPacketInterceptor.class);

    public static final String NAMESPACE = "urn:xmpp:raa:0#embed-presence-sub";

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

        if (!(packet instanceof Presence) || ((Presence) packet).getType() != Presence.Type.subscribe) {
            // This implementation is only acting on presence subscribe requests.
            return;
        }

        // Protect against our own clients trying to spoof. The XEP dictates that the server MUST strip
        // client-originating <info/> elements.
        final boolean didRemoveSomething = packet.deleteExtension("info", Info.NAMESPACE);
        if (didRemoveSomething) {
            Log.info("Prevented RAA spoofing: Removed RAA 'info' extension from presence stanza of type {} sent by {} addressed to: {}", ((Presence) packet).getType(), packet.getFrom(), packet.getTo());
        }

        final JID originator = packet.getFrom();
        if (originator == null) {
            // This should not be possible, right? Be verbose if the unexpected does occur!
            Log.warn("Unable to process presence subscription stanza, as the stanza has no 'from' attribute: {}", packet);
        } else {
            Log.trace("Attempting to add info to presence subscription stanza for user '{}'", originator);
            final Info info = InfoDAO.lookup(originator);
            if (info != null) {
                packet.getElement().add(info.asElement());
            }
        }
    }
}
