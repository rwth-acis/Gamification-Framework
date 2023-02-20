FROM openjdk:17
WORKDIR /
ADD target/successawarenessmodel_webapp-1.0-SNAPSHOT.jar app.jar
RUN useradd -m las2peer
USER las2peer
EXPOSE 10002
CMD java -jar -Dspring.profiles.active=prod app.jar
