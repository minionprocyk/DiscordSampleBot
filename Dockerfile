FROM gradle:5.2.1-jdk8-slim
COPY . /opt/DiscordSampleBot
WORKDIR /opt/DiscordSampleBot

USER root
RUN chown -R gradle /opt/DiscordSampleBot

USER gradle

RUN gradle build
