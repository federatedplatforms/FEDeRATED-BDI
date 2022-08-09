FROM azul/zulu-openjdk:8
ADD build/libs/clients-0.1.jar clients.jar
EXPOSE 10050
CMD java -Dserver.port=$springPort -Dconfig.rpc.host=$cordaHost -Dconfig.rpc.port=$cordaPort -Dconfig.rpc.username=$cordaUser -Dconfig.rpc.password=$cordaPassword -jar clients.jar

