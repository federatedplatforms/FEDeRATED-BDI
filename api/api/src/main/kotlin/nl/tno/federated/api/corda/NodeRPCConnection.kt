package nl.tno.federated.api.corda

import jakarta.annotation.PreDestroy
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCClientConfiguration
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.seconds
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Wraps an RPC connection to a Corda node.
 *
 * The RPC connection is configured using command line arguments.
 *
 * Retries the connection every 30 secs.
 *
 * @param host The host of the node we are connecting to.
 * @param rpcPort The RPC port of the node we are connecting to.
 * @param username The username for logging into the RPC client.
 * @param password The password for logging into the RPC client.
 * @property proxy The RPC proxy.
 */
@Component
class NodeRPCConnection(
    @Value("\${federated.node.corda.rpc.host}") private val host: String,
    @Value("\${federated.node.corda.rpc.username}") private val username: String,
    @Value("\${federated.node.corda.rpc.password}") private val password: String,
    @Value("\${federated.node.corda.rpc.port}") private val rpcPort: Int
) {
    private val log = LoggerFactory.getLogger(NodeRPCConnection::class.java)
    private var rpcConnection: CordaRPCConnection? = null

    private val cordaRPCOps: CordaRPCOps by lazy {
        log.debug("Initializing CordaRPCConnection host: $host, port: $rpcPort, user: $username")
        val rpcAddress = NetworkHostAndPort(host, rpcPort)
        val rpcClient = CordaRPCClient(rpcAddress, CordaRPCClientConfiguration(connectionMaxRetryInterval = 30.seconds))
        val rpcConnection = rpcClient.start(username, password)
        log.debug("Initializing CordaRPCConnection successful!!")

        rpcConnection.proxy
    }

    fun client() = try {
        cordaRPCOps
    } catch (e: Exception) {
        log.error("Failed to initialize CordaRPCOps: ${e.message}")
    }

    @PreDestroy
    fun close() {
        rpcConnection?.notifyServerAndClose()
    }
}