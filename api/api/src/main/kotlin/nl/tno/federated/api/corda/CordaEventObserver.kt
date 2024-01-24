package nl.tno.federated.api.corda

import jakarta.annotation.PostConstruct
import nl.tno.federated.api.webhook.GenericEvent
import nl.tno.federated.corda.states.EventState
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import rx.schedulers.Schedulers

@Service
class CordaEventObserver(private val rpc: NodeRPCConnection, private val applicationEventPublisher: ApplicationEventPublisher) {

    private val log = LoggerFactory.getLogger(CordaEventObserver::class.java)
    private var subscribed = false

//    @PostConstruct
    fun observe() {

        while (!subscribed) {
            try {
                val feed = rpc.client().vaultTrack(EventState::class.java)
                feed.updates.observeOn(Schedulers.io()).subscribe { update ->
                    log.info("Received notification: {}", update)
                    update.produced.forEach { stateAndRef ->
                        // No need to launch coroutines here because the applicationEventPublisher is async.
                        stateAndRef.state.data.let { applicationEventPublisher.publishEvent(GenericEvent(it.eventType, it.event)) }
                    }
                }
                subscribed = true
            } catch (e: Exception) {
                log.warn("Failed listening for updates... sleeping for 5 seconds. Error: {}", e.message)
                subscribed = false
                Thread.sleep(5000)
            }
        }
    }
}
