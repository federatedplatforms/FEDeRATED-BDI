FROM azul/zulu-openjdk:8
ADD ./cordformation/build/nodes /opt/corda/
EXPOSE 10006
CMD cd /opt/corda/SingleNode_SN && java -Dname=SingleNode_SN -jar corda.jar