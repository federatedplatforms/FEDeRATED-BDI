package nl.tno.federated.api.distribution

interface EventDistributionService<in T> {

    /**
     * This function determines based on the incoming event which node(s) to send the event to.
     * It returns a set containing the node destinations where to send this event to.
     */
    fun extractDestinationsFromEvent(event: T): Set<EventDestination>
}


