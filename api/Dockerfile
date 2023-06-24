FROM azul/zulu-openjdk:8
ADD build/libs/api.jar api.jar
EXPOSE ${SERVER_PORT}
CMD java -Dserver.port=${SERVER_PORT} -corda.rpc.host=${CORDA_RPC_HOST} -corda.rpc.port=${CORDA_RPC_PORT} -corda.rpc.username=${CORDA_RPC_USER} -corda.rpc.password=${CORDA_RPC_PASSWORD} -jar api.jar