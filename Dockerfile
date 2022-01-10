FROM azul/zulu-openjdk:8
ADD build/nodes/SingleNode_SN SingleNode_SN/
EXPOSE 10005 10006
CMD java -jar corda.jar

