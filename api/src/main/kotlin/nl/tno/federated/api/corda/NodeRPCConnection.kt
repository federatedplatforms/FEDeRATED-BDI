package nl.tno.federated.api.corda

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 *
 * @param host The host of the node we are connecting to.
 * @param rpcPort The RPC port of the node we are connecting to.
 * @param username The username for logging into the RPC client.
 * @param password The password for logging into the RPC client.
 * @property proxy The RPC proxy.
 */
@Component
class NodeRPCConnection(
    @Value("\${config.rpc.host}") private val host: String,
    @Value("\${config.rpc.username}") private val username: String,
    @Value("\${config.rpc.password}") private val password: String,
    @Value("\${config.rpc.port}") private val rpcPort: Int
) {
    private val log = LoggerFactory.getLogger(NodeRPCConnection::class.java)
    private var rpcConnection: CordaRPCConnection? = null

    private val cordaRPCOps: CordaRPCOps by lazy {
        log.debug("Initializing CordaRPCConnection host: $host, port: $rpcPort, user: $username")
        val rpcAddress = NetworkHostAndPort(host, rpcPort)
        val rpcClient = CordaRPCClient(rpcAddress)
        val rpcConnection = rpcClient.start(username, password)
        log.debug("Initializing CordaRPCConnection successful!!")

        rpcConnection.proxy
    }

    fun client() = cordaRPCOps

    @PreDestroy
    fun close() {
        rpcConnection?.notifyServerAndClose()
    }
}