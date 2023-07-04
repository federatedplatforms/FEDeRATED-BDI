package nl.tno.federated.api.controllers

import nl.tno.federated.api.corda.NodeRPCConnection
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.rules.EventDistributionRule
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class IndexController(
    private val environment: Environment,
    private val rules: Set<EventDistributionRule<CordaEventDestination>>,
    private val rpc: NodeRPCConnection
) {

    companion object {
        private val log = LoggerFactory.getLogger(IndexController::class.java)
    }

    @RequestMapping("/")
    fun index(model: Model): String {
        model.addAttribute("graphdbSparqlUrl", environment.getProperty("graphdb.sparql.url"))
        model.addAttribute("eventDistributionRules", getDistributionRules())
        model.addAttribute("cordaRpcUrl", environment.getProperty("corda.rpc.host") + ":" + environment.getProperty("corda.rpc.port") )
        model.addAttribute("notaries", getNotaries())
        model.addAttribute("peers", getPeers())
        return "index"
    }

    private fun getDistributionRules() = rules.joinToString { it.javaClass.simpleName }

    private fun getNotaries() : String {
        return try {
            rpc.client().notaryIdentities().map { it.name }.joinToString()
        } catch (e: Exception) {
            log.warn(e.message, e)
            "ERROR: Cant retrieve information about notaries... Corda RPC URL not reachable? Please see logs for details."
        }
    }

    private fun getPeers() : String {
        return try {
            rpc.client().networkMapSnapshot().flatMap { it.legalIdentities }.map { it.name }.joinToString()
        } catch (e: Exception) {
            log.warn(e.message, e)
            "ERROR: Cant retrieve information about peers... Corda RPC URL not reachable? Please see logs for details."
        }
    }
}