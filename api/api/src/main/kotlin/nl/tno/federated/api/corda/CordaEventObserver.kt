package nl.tno.federated.api.corda

import nl.tno.federated.api.event.query.corda.CordaEventQueryService
import nl.tno.federated.api.webhook.GenericEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * The CordaEventObserver retrieves events from the ledger/vault since the last successful poll interval.
 *
 * Initial value for last poll interval is set to the startup time of the application. This means that
 * during downtime of this application, no events are sent to Webhooks, neither are events that were
 * received during the downtime.
 */
@Service
class CordaEventObserver(private val eventQueryService: CordaEventQueryService, private val applicationEventPublisher: ApplicationEventPublisher) {

    private val log = LoggerFactory.getLogger(CordaEventObserver::class.java)
    private var lastPoll:  Instant = Instant.now()

    @Scheduled(fixedDelay = 15_000, initialDelay = 15_000)
    fun observe() {
        try {
            log.info("Retrieving events for publication since last successful poll interval: {}", lastPoll)
            val result = eventQueryService.findAfter(lastPoll,1, 500)

            log.info("{} events retrieved from the ledger/vault for publication.", result.size)

            if(result.isNotEmpty()) {

                result.forEach {
                    log.info("Publishing event...")
                    applicationEventPublisher.publishEvent(GenericEvent(it.eventType, it.eventData, it.eventUUID))
                }
                // Update the last poll timestamp to last recordedTime from list of events
                lastPoll = lastRecordedTimestamp(result).recordedTime
            }
        }
        catch (e: Exception) {
            log.warn("Failed to fetch events for publication: {}", e.message)
        }
    }

    private fun lastRecordedTimestamp(list: List<SimpleEventState>): SimpleEventState {
        return list.maxBy { it.recordedTime }
    }
}