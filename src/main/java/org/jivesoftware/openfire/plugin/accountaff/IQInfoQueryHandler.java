/*
 * Copyright (C) 2023-2025 Ignite Realtime Foundation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.openfire.plugin.accountaff;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.admin.AdminManager;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

/**
 * An IQ Handler that processes IQ requests sent to the server that contain queries related to the protocol described
 * in XEP-0489: Reporting Account Affiliations.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 * @see <a href="https://xmpp.org/extensions/xep-0489.html">XEP-0489: Reporting Account Affiliations</a>
 */
public class IQInfoQueryHandler extends IQHandler
{
    private static final Logger Log = LoggerFactory.getLogger(IQInfoQueryHandler.class);

    private final IQHandlerInfo info;

    public IQInfoQueryHandler()
    {
        super("Reporting Account Affiliations handler");
        this.info = new IQHandlerInfo("query", Info.NAMESPACE);
    }

    @Override
    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        Log.trace("Processing RAA IQ stanza from {}", packet.getFrom());
        if (packet.isResponse()) {
            Log.debug("Silently ignoring IQ response stanza from {}", packet.getFrom());
            return null;
        }

        final IQ reply = IQ.createResultIQ(packet);
        reply.setChildElement(packet.getChildElement().createCopy());

        if (IQ.Type.set == packet.getType()) {
            Log.debug("Returning error to {}: request is of incorrect IQ type.", packet.getFrom());
            reply.setError(PacketError.Condition.feature_not_implemented);
            return reply;
        }

        final JID target = packet.getTo();

        Log.trace("Processing RAA query request from {} for {}", packet.getFrom(), target);
        final Info info = InfoDAO.lookup(target);
        if (info == null) {
            Log.trace("Unable to find info for account {}. Returning item-not-found to {}.", target, packet.getFrom());
            reply.setError(PacketError.Condition.item_not_found);
        } else {
            Log.trace("Found info for account {}. Returning info to {}: {}", target, packet.getFrom(), info);
            reply.getChildElement().add(info.asElement());
        }
        return reply;
    }

    @Override
    public IQHandlerInfo getInfo()
    {
        return info;
    }
}
