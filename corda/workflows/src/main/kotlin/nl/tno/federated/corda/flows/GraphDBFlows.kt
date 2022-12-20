package nl.tno.federated.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowExternalOperation
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import nl.tno.federated.corda.services.graphdb.GraphDBCordaService
import org.slf4j.LoggerFactory

@InitiatingFlow
@StartableByRPC
class QueryGraphDBbyIdFlow(
    val id: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return await(GraphDBQueryById(graphdb(), id))
    }
}

@InitiatingFlow
@StartableByRPC
class GeneralSPARQLqueryFlow(
    val query: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return await(GraphDBGeneralSPARQLquery(graphdb(), query))
    }
}

@InitiatingFlow
@StartableByRPC
class InsertRDFFlow(
    val rdfEvent: String,
    val privateRepo: Boolean
) : FlowLogic<Boolean>() {

    @Suspendable
    override fun call(): Boolean {
        return await(GraphDBInsert(graphdb(), rdfEvent, privateRepo))
    }
}

/**
 * https://docs.r3.com/en/platform/corda/4.9/community/api-flows.html#calling-external-systems-inside-of-flows
 */
class GraphDBInsert(
    private val graphDBCordaService: GraphDBCordaService,
    private val rdfEvent: String,
    private val privateRepo: Boolean
) : FlowExternalOperation<Boolean> {

    private val log = LoggerFactory.getLogger(GraphDBInsert::class.java)

    // Implement [execute] which will be run on a thread outside of the flow's context
    override fun execute(deduplicationId: String): Boolean {
        return graphDBCordaService.insertEvent(rdfEvent, privateRepo).also {
            log.info("Insert into GraphDB returned: {}", it)
        }
    }
}

class GraphDBGeneralSPARQLquery(
    private val graphDBCordaService: GraphDBCordaService,
    private val query: String
) : FlowExternalOperation<String> {

    override fun execute(deduplicationId: String): String {
        return graphDBCordaService.generalSPARQLquery(query)
    }
}

class GraphDBQueryById(
    private val graphDBCordaService: GraphDBCordaService,
    private val id: String
) : FlowExternalOperation<String> {

    override fun execute(deduplicationId: String): String {
        return graphDBCordaService.queryEventById(id)
    }
}

fun FlowLogic<*>.graphdb() = serviceHub.cordaService(GraphDBCordaService::class.java)