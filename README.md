# CerberusLink

A Minecraft plugin to link DiscordSRV to Cerberus.

## How does it work?

Every minute, the plugin uses DiscordSRV to see which users are online and have their Discord accounts linked, and sends those users as a POST request to Cerberus' webserver. From there, it adds the users to the database for the given server.
