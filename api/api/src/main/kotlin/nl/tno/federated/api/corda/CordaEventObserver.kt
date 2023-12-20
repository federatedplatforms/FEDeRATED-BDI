package nl.tno.federated.api.corda

import nl.tno.federated.api.webhook.GenericEvent
import nl.tno.federated.shared.states.EventState
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import rx.schedulers.Schedulers

@Service
class CordaEventObserver(private val rpc: NodeRPCConnection, private val applicationEventPublisher: ApplicationEventPublisher) {

    // TODO when to trigger this? On application startup?
    fun observe() {
        val feed = rpc.client().vaultTrack(EventState::class.java)
        feed.updates.observeOn(Schedulers.io()).subscribe { update ->
            update.produced.forEach { stateAndRef ->
                // No need to launch coroutines here because the applicationEventPublisher is async.
                stateAndRef.state.data.let { applicationEventPublisher.publishEvent(GenericEvent(it.eventType, it.event)) }
            }
        }
    }
}
