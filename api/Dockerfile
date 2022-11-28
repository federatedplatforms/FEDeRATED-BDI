FROM azul/zulu-openjdk:8
ADD build/libs/api.jar api.jar
EXPOSE 10050
CMD java -Dserver.port=10050 -Dconfig.rpc.host=localhost -Dconfig.rpc.port=10006 -Dconfig.rpc.username=user1 -Dconfig.rpc.password=vzzuABeCut3jGoJfEp94 -jar api.jar

