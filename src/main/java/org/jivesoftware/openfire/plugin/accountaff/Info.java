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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.util.XMPPDateTimeFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Representation of account information, as defined in XEP-0489: Reporting Account Affiliations.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 * @see <a href="https://xmpp.org/extensions/xep-0489.html">XEP-0489: Reporting Account Affiliations</a>
 */
public class Info
{
    public static final String NAMESPACE = "urn:xmpp:raa:0";

    private final Affiliation affiliation;
    private final Instant since;
    private final Integer trust;

    public Info(@Nonnull final Affiliation affiliation, @Nullable final Instant since, @Nullable final Integer trust)
    {
        this.affiliation = affiliation;

        // Security consideration: If a server chooses to expose an account’s creation timestamp to untrusted entities,
        // the reported value SHOULD be approximate - e.g. rounded to the day on which the account registered - to
        // preserve privacy. Providing a value with a high precision may allow entities to correlate the account
        // registration with other actions performed by the user, or determine a user’s likely time zone.
        this.since = since == null ? null : since.truncatedTo(ChronoUnit.DAYS);

        if (trust != null && (trust < 0 || trust > 100)) {
            throw new IllegalArgumentException("Argumenet 'trust' must be null, or a value between 0 and 100 (inclusive). It was: " + trust);
        }
        this.trust = trust;
    }

    /**
     * Returns an XML element that represents the agent.
     *
     * @return an XML element.
     */
    public Element asElement()
    {
        final Element result = DocumentHelper.createElement(QName.get("info", NAMESPACE));
        result.addAttribute("affiliation", affiliation.toString().toLowerCase());
        if (since != null) {
            result.addAttribute("since", XMPPDateTimeFormat.format(Date.from(since)));
        }
        if (trust != null) {
            result.addAttribute("trust", String.valueOf(trust));
        }
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Info info = (Info) o;
        return affiliation == info.affiliation && Objects.equals(since, info.since) && Objects.equals(trust, info.trust);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(affiliation, since, trust);
    }

    @Override
    public String toString()
    {
        return "Info{" +
            "affiliation=" + affiliation +
            ", since=" + since +
            ", trust=" + trust +
            '}';
    }
}
