FROM azul/zulu-openjdk:8
ADD build/libs/api.jar api.jar
EXPOSE 10050
CMD java -Dserver.port=10050 -Dcorda.rpc.host=localhost -Dcorda.rpc.port=10006 -Dcorda.rpc.username=user1 -Dcorda.rpc.password=vzzuABeCut3jGoJfEp94 -jar api.jar