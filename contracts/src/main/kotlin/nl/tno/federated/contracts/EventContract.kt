package nl.tno.federated.contracts

import net.corda.core.contracts.*
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

        val outputStates = tx.outputStates
        val inputStates = tx.inputStates.filterIsInstance<EventState>()
        val referenceStates = tx.referenceStates.filterIsInstance<EventState>()

        val eventState = outputStates.filterIsInstance<EventState>().single()

        when(command.value) {
            is Commands.Create -> {
                requireThat{
                    "There must be exactly one timestamp at time of creation" using (eventState.timestamps.size == 1)
                    "The type of the timestamp must be either PLANNED or ACTUAL" using (
                            when(eventState.timestamps.single().type) {
                                TimeType.PLANNED, TimeType.ACTUAL -> true
                                else -> false
                            }
                            )
                }
                // TODO other general creation constraints?

                when(eventState.milestone) {
                    Milestone.START -> {
                        requireThat{
                            "Goods and locations cannot be linked together" using (!(eventState.goods.isNotEmpty() && eventState.location.isNotEmpty()))
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
                            "There must be 1 previous event" using (inputStates.size == 1)
                            "Previous event must be of type START" using (inputStates.single().milestone == Milestone.START)
                            "Digital twins in the previous START event must equal to those in the current STOP event" using (
                                            inputStates.single().hasSameDigitalTwins(eventState)
                                    )
                            // TODO check last timestamp of START is EXECUTED
                        }
                        // TODO other constraints for stop case?
                    }
                }
            }

            is Commands.UpdateEstimatedTime, is Commands.ExecuteEvent -> {
                requireThat{
                    "There must be a previous corresponding event as input" using (inputStates.isNotEmpty())
                    "Besides times, id and participants, input and output states must be equal" using (inputStates.single().equals(eventState))
                    val oldTimestamps = inputStates.single().timestamps
                    val newTimestamps = eventState.timestamps
                    "Last element of old timestamps cannot be of type ACTUAL" using (oldTimestamps.last().type != TimeType.ACTUAL)
                    "First element of old timestamps must be of type PLANNED" using (oldTimestamps.first().type == TimeType.PLANNED)
                    "Old timestamps and new timestamps must be equal, net of the last element" using (oldTimestamps == newTimestamps - newTimestamps.last())
                }
            }

            is Commands.UpdateEstimatedTime -> {
                val newTimestamps = eventState.timestamps
                requireThat{
                    "The last (added) timestamp must be of type ESTIMATED" using (newTimestamps.last().type == TimeType.ESTIMATED)
                }
            }

            is Commands.ExecuteEvent -> {
                val newTimestamps = eventState.timestamps
                requireThat{
                    "The last (added) timestamp must be of type ACTUAL" using (newTimestamps.last().type == TimeType.ACTUAL)
                }

                when(eventState.milestone) {
                    Milestone.STOP -> {
                        requireThat{
                            "There must be only one previous START event" using (referenceStates.size == 1)
                            val correspondingStartEvent = referenceStates.single()
                            "The corresponding START event must involve the same digital twins" using (correspondingStartEvent.hasSameDigitalTwins(eventState))
                            "The last timestamps of the corresponding START event must be of type ACTUAL" using (
                                    correspondingStartEvent.timestamps.last().type == TimeType.ACTUAL )
                        }
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