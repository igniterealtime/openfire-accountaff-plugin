# Openfire Reporting Account Affiliations Plugin

This is a plugin for the Openfire Real-time Communications server.     The Reporting Account Affiliations plugin provides a way for Openfire to report to other entities the relationship it has with a user
on its domain, as specified in [XEP-0489: Reporting Account Affiliations](https://xmpp.org/extensions/xep-0489.html).

Note: at the time of writing, the protocol as implemented by this plugin has not yet been accepted for consideration or approved 
in any official manner by the XMPP Standards Foundation, and this document is not yet an XMPP Extension Protocol (XEP). This plugin should
be considered experimental.

## Using the 'member' affiliation

The Reporting Account Affiliations specification defines four 'affiliation' types:

- **anonymous**: the address belongs to an anonymous, temporary or guest account. The user is not known to the server.
- **registered**: the address belongs to an account that self-registered, e.g. using XEP-0077
- **member**: the address belongs to a trusted member of the server - e.g. accounts that are provisioned for known users.
- **admin**: the address belongs to a server administrator

This plugin is able to automatically determine the correct affiliation value for a user, but will (without manual configuration)
never resolve the 'member' affiliation: Openfire does not keep track of how an account was registered.

To make this plugin report an account as having the `member` affiliation, you can configure the corresponding user to
have a User Property with the name <code>RAA Affiliation</code> that has the value <code>member</code>

User properties can be edited on the Admin Console when the [RawPropertyEditor plugin](https://www.igniterealtime.org/projects/openfire/plugin-archive.jsp?plugin=rawpropertyeditor)
has been installed.

## Reporting Issues

Issues may be reported to the [forums](https://discourse.igniterealtime.org) or via this repo's [Github Issues](https://github.com/igniterealtime/openfire-accountaff-plugin).
