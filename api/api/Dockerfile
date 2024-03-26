FROM openjdk:17
ADD build/libs/api.jar api.jar
EXPOSE 10050
CMD java -Dserver.port=10050 -Dfederated.node.corda.rpc.host=localhost -Dfederated.node.corda.rpc.port=10006 -Dfederated.node.corda.rpc.username=user1 -Dfederated.node.corda.rpc.password=vzzuABeCut3jGoJfEp94 -jar api.jar