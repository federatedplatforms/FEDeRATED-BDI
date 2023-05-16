FROM azul/zulu-openjdk:8
ADD build/libs/api.jar api.jar
EXPOSE ${SERVER_PORT}
CMD java -Dserver.port=${SERVER_PORT} -Dconfig.rpc.host=${CORDA_HOST} -Dconfig.rpc.port=${CORDA_PORT} -Dconfig.rpc.username=${CORDA_USER} -Dconfig.rpc.password=${CORDA_PASSWORD} -jar api.jar