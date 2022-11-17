package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import nl.tno.federated.services.CordaGraphDBService
import nl.tno.federated.states.Event

@InitiatingFlow
@StartableByRPC
class QueryGraphDBbyIdFlow(
    val id: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
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
        return graphdb().generalSPARQLquery(query)
    }
}

@InitiatingFlow
@StartableByRPC
class ParseRDFFlow(
    val rdfEvent: String
) : FlowLogic<List<Event>>() {

    @Suspendable
    override fun call(): List<Event> {
        return graphdb().parseRDFToEvents(rdfEvent)
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
        return graphdb().insertEvent(rdfEvent, privateRepo)
    }
}

fun FlowLogic<*>.graphdb() = serviceHub.cordaService(CordaGraphDBService::class.java)