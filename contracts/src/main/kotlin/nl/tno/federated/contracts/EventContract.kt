package nl.tno.federated.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.*

// ************
// * Contract *
// ************
class EventContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "nl.tno.federated.contracts.EventContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        val inputStates = tx.inputStates.filterIsInstance<EventState>()
        val outputStates = tx.outputStates
        val referenceStates = tx.referenceStates.filterIsInstance<EventState>()

        val eventState = outputStates.filterIsInstance<EventState>().single()

        when(command.value) {
            is Commands.Create -> {
                requireThat{
                    "There must be exactly one timestamp at time of creation" using (eventState.timestamps.size == 1)
                    "The type of the timestamp must be PLANNED or ACTUAL" using (
                            eventState.timestamps.single().type == EventType.PLANNED || eventState.timestamps.single().type == EventType.ACTUAL)
                }

                when(eventState.milestone) {
                    Milestone.START -> {
                        requireThat{
                            "Goods and locations cannot be linked together" using !(eventState.goods.isNotEmpty() && eventState.location.isNotEmpty())
                            "There can be one location only" using (eventState.location.size <= 1)
                            "There can be one good only" using (eventState.goods.size <= 1)
                            "There can be one transport mean only" using (eventState.transportMean.size <= 1)
                            // there can be one "other" only?
                            "There must be at least a connection" using (
                                            eventState.goods.size +
                                            eventState.location.size +
                                            eventState.transportMean.size +
                                            eventState.otherDigitalTwins.size >= 2)
                            // requirements about eCMR uri?
                        }
                    }

                    Milestone.STOP -> {
                        requireThat{
                            "There must be one previous event" using (referenceStates.size == 1)
                            "Previous event must be of type START" using (referenceStates.single().milestone == Milestone.START)
                            "Digital twins in the previous START event must equal to those in the current STOP event" using (
                                        referenceStates.single().hasSameDigitalTwins(eventState)
                                    )
                        }
                    }
                }
            }

            is Commands.UpdateEstimatedTime -> {
                requireThat{
                    "There must be a previous corresponding event as input" using (inputStates.size == 1)
                    "Besides times, id and participants, input and output states must be equal" using (inputStates.single().equalsExceptTimesAndParticipants(eventState))
                    val oldTimestamps = inputStates.single().timestamps
                    val newTimestamps = eventState.timestamps
                    "First element of old timestamps must be of type PLANNED" using (oldTimestamps.first().type == EventType.PLANNED)
                    "Last element of old timestamps cannot be of type ACTUAL" using (oldTimestamps.last().type != EventType.ACTUAL)
                    "Old timestamps and new timestamps must be equal, net of the new element" using (oldTimestamps == newTimestamps-newTimestamps.last())
                    "The last (added) timestamp must be of type ESTIMATED" using (newTimestamps.last().type == EventType.ESTIMATED)
                }
            }

            is Commands.ExecuteEvent -> {
                val correspondingEvent = inputStates.filter{
                    it.milestone == eventState.milestone
                    /* This filtering is to leave out previous states with
                    * a different milestone, which should happen only in the case
                    * of executing a STOP event, where in the flow also the START
                    * event is passed. */
                }
                val correspondingStartEvent = inputStates - correspondingEvent

                requireThat{
                    "There must be a previous corresponding event as input" using (correspondingEvent.isNotEmpty())
                    "Besides times, id and participants, input and output states must be equal" using (correspondingEvent.single().equalsExceptTimesAndParticipants(eventState))
                    val oldTimestamps = correspondingEvent.single().timestamps
                    val newTimestamps = eventState.timestamps
                    "Last element of old timestamps cannot be of type ACTUAL" using (oldTimestamps.last().type != EventType.ACTUAL)
                    "First element of old timestamps must be of type PLANNED" using (oldTimestamps.first().type == EventType.PLANNED)
                    "The last (added) timestamp must be of type ACTUAL" using (newTimestamps.last().type == EventType.ACTUAL)
                    "Old timestamps and new timestamps must be equal, net of the new element" using (oldTimestamps == newTimestamps-newTimestamps.last())
                }

                if (eventState.milestone == Milestone.STOP) {
                    requireThat{
                        "There must be only one previous START event" using (correspondingStartEvent.size == 1)
                        "The corresponding START event must involve the same digital twins" using (correspondingStartEvent.single().hasSameDigitalTwins(eventState))
                        "The last timestamps of the corresponding START event must be of type ACTUAL" using (
                                correspondingStartEvent.single().timestamps.last().type == EventType.ACTUAL )
                    }
                }
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Other : Commands
        class Create : Commands
        class UpdateEstimatedTime : Commands
        class ExecuteEvent : Commands
    }
}

