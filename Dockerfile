FROM maven:3.6.1-jdk-11-slim AS build
COPY src /src
COPY pom.xml /
COPY .git /.git
RUN mvn --quiet -B -f /pom.xml clean package

FROM openjdk:11-jre
COPY --from=build /target/espn_player_scraper.jar espn_player_scraper.jar
ENTRYPOINT exec java $JAVA_OPTIONS -jar /espn_player_scraper.jar
