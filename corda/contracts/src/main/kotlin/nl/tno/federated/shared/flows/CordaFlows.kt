package nl.tno.federated.shared.flows

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
abstract class NewEventFlow(
    val destinations: Collection<CordaX500Name>,
    val event: String,
    val eventType: String,
    val eventUUID: String
) : FlowLogic<SignedTransaction>()

@InitiatingFlow
@StartableByRPC
abstract class DataPullFlow(val destination: CordaX500Name,
                            val query: String) : FlowLogic<SignedTransaction>()