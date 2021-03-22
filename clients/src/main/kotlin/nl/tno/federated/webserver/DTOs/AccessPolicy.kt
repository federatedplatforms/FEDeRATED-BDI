package nl.tno.federated.webserver.DTOs

import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.states.IdsAction
import nl.tno.federated.states.Target


@CordaSerializable
data class AccessPolicy(
    val context : String,
    val type : String,
    val id : String,
    val idsProvider : String,
    val idsConsumer : String,
    val idsPermission : List<IdsAction>,
    val idsTarget : Target
)