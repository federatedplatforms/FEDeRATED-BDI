package nl.tno.federated.api.corda

import nl.tno.federated.api.event.query.corda.CordaEventQueryService
import nl.tno.federated.api.webhook.GenericEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CordaEventObserver(private val eventQueryService: CordaEventQueryService, private val applicationEventPublisher: ApplicationEventPublisher) {

    private val log = LoggerFactory.getLogger(CordaEventObserver::class.java)

    @Scheduled(fixedDelay = 10_000, initialDelay = 5_000)
    fun observe() {
        try {
            log.info("Retrieving events for publication...")
            val findAll = eventQueryService.findAll(1, 100)
            log.info("{} events available for publication...", findAll.size)
            findAll.forEach {
                log.info("Publishing event...")
                applicationEventPublisher.publishEvent(GenericEvent(it.eventType, it.eventData))
            }
        }
        catch (e: Exception) {
            log.warn("Failed to fetch events for publication: {}", e.message)
        }
    }
}
