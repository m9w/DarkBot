# DarkBOT

Simplified development of extensions.
Example: https://github.com/m9w/Plugin-Launcher-Darkbot

Features:
- Support autologin by `sid` and `server` - just put this props to -login file. If sid is invalid than it be replaced on actual after default autologin.

How to run:
- Clone the repository
- Get the latest full-release on darkbot's discord https://discord.gg/uXHnZJ9
- Unzip the release in a known folder outside of the project
- Run `mvn clean install`
- Add Run/Debug configuration:
  - Main class: com.github.manolo8.darkbot.Bot
  - Working directory: wherever you unzipped the release
- For build jar run `mvn compile package`

Distribution & support for the bot can be found over at discord: https://discord.gg/bEFgxCy

Everyone is allowed to make, publish & redistribute videos & content about the software.

Bugpoint is not affiliated in any way with this software. They claim themselves as the owners in DMCA claims, which are all invalid.
