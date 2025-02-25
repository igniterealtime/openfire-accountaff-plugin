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

/**
 * Definition of all account affiliations, as specified by the XEP.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 * @see <a href="https://xmpp.org/extensions/xep-0489.html">XEP-0489: Reporting Account Affiliations</a>
 */
public enum Affiliation
{
    /**
     * The address belongs to an anonymous, temporary or guest account. The user is not known to the server.
     */
    anonymous,

    /**
     * The address belongs to an account that self-registered, e.g. using XEP-0077
     */
    registered,

    /**
     * The address belongs to a trusted member of the server - e.g. accounts that are provisioned for known users.
     */
    member,

    /**
     * The address belongs to a server administrator.
     */
    admin
}
