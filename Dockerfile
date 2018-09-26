FROM ubuntu:18.0.4
COPY . /opt/DiscordSampleBot
CMD /opt/DiscordSampleBot/gradlew run
