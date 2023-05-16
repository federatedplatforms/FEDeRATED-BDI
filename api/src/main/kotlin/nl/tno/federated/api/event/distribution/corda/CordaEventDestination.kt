package nl.tno.federated.api.event.distribution.corda

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.api.event.distribution.EventDestination

class CordaEventDestination(destination: CordaX500Name)
    : EventDestination<CordaX500Name>(destination)