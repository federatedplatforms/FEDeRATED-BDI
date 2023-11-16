package nl.tno.federated.corda.flows

import net.corda.core.flows.FlowLogic
import net.corda.core.transactions.SignedTransaction

abstract class IDataPullQueryFlow : FlowLogic<SignedTransaction>()