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

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.interceptor.InterceptorManager;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

/**
 * An Openfire plugin that implements XEP-0489: Reporting Account Affiliations.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 * @see <a href="https://xmpp.org/extensions/xep-0489.html">XEP-0489: Reporting Account Affiliations</a>
 */
public class ReportingAccountAffiliationsPlugin implements Plugin
{
    private IQInfoQueryHandler handler;

    private EmbedPresenceSubPacketInterceptor embedPresenceSubPacketInterceptor;
    private EmbedPresenceDirectedPacketInterceptor embedPresenceDirectedPacketInterceptor;
    private EmbedMessagePacketInterceptor embedMessagePacketInterceptor;

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        handler = new IQInfoQueryHandler();
        XMPPServer.getInstance().getIQRouter().addHandler(handler);
        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature(Info.NAMESPACE);

        embedPresenceSubPacketInterceptor = new EmbedPresenceSubPacketInterceptor();
        InterceptorManager.getInstance().addInterceptor(embedPresenceSubPacketInterceptor);
        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature(EmbedPresenceSubPacketInterceptor.NAMESPACE);

        embedPresenceDirectedPacketInterceptor = new EmbedPresenceDirectedPacketInterceptor();
        InterceptorManager.getInstance().addInterceptor(embedPresenceDirectedPacketInterceptor);
        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature(EmbedPresenceDirectedPacketInterceptor.NAMESPACE);

        embedMessagePacketInterceptor = new EmbedMessagePacketInterceptor();
        InterceptorManager.getInstance().addInterceptor(embedMessagePacketInterceptor);
        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature(EmbedMessagePacketInterceptor.NAMESPACE);
    }

    @Override
    public void destroyPlugin()
    {
        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(EmbedMessagePacketInterceptor.NAMESPACE);
        if (embedMessagePacketInterceptor != null) {
            InterceptorManager.getInstance().removeInterceptor(embedMessagePacketInterceptor);
            embedMessagePacketInterceptor = null;
        }

        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(EmbedPresenceDirectedPacketInterceptor.NAMESPACE);
        if (embedPresenceDirectedPacketInterceptor != null) {
            InterceptorManager.getInstance().removeInterceptor(embedPresenceDirectedPacketInterceptor);
            embedPresenceDirectedPacketInterceptor = null;
        }

        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(EmbedPresenceSubPacketInterceptor.NAMESPACE);
        if (embedPresenceSubPacketInterceptor != null) {
            InterceptorManager.getInstance().removeInterceptor(embedPresenceSubPacketInterceptor);
            embedPresenceSubPacketInterceptor = null;
        }

        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(Info.NAMESPACE);
        if (handler != null) {
            XMPPServer.getInstance().getIQRouter().removeHandler(handler);
            handler = null;
        }
    }
}
