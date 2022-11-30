package nl.tno.federated.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowExternalOperation
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import nl.tno.federated.corda.services.graphdb.GraphDBCordaService
import nl.tno.federated.corda.services.ishare.ISHARECordaService
import nl.tno.federated.states.Event

@InitiatingFlow
@StartableByRPC
class QueryGraphDBbyIdFlow(
    val id: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        // TODO https://docs.r3.com/en/platform/corda/4.9/community/api-flows.html#calling-external-systems-inside-of-flows
        return graphdb().queryEventById(id)
    }
}

@InitiatingFlow
@StartableByRPC
class GeneralSPARQLqueryFlow(
    val query: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        // TODO https://docs.r3.com/en/platform/corda/4.9/community/api-flows.html#calling-external-systems-inside-of-flows
        return graphdb().generalSPARQLquery(query)
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
        // TODO https://docs.r3.com/en/platform/corda/4.9/community/api-flows.html#calling-external-systems-inside-of-flows
        return graphdb().insertEvent(rdfEvent, privateRepo)
    }
}

fun FlowLogic<*>.graphdb() = serviceHub.cordaService(GraphDBCordaService::class.java)