FROM azul/zulu-openjdk:8
ADD build/libs/semantic-adapter-standalone.jar semantic-adapter-standalone.jar
EXPOSE 1337
CMD java -Dserver.port=8080 -jar semantic-adapter-standalone.jar