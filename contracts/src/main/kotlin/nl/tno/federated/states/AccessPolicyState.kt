package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.DigitalTwinContract

@BelongsToContract(DigitalTwinContract::class)
data class AccessPolicyState(
    val accessPolicy: AccessPolicy,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is AccessPolicySchemaV1) {

            val pAssetRefinement = accessPolicy.idsTarget.idsAssetRefinement.let {
                AccessPolicySchemaV1.PersistentAssetRefinement(
                    UniqueIdentifier().id,
                    it.type,
                    it.idsLeftOperand,
                    it.idsOperator,
                    it.idsRightOperand
                )
            }

            val pTarget = accessPolicy.idsTarget.let {
                AccessPolicySchemaV1.PersistentTarget(
                    UniqueIdentifier().id,
                    it.type,
                    pAssetRefinement
                )
            }

            val pAction = accessPolicy.idsPermission.map {
                AccessPolicySchemaV1.PersistentAction(
                    UniqueIdentifier().id,
                    it.id
                )
            }

            return AccessPolicySchemaV1.PersistentAccessPolicy(
                linearId.id,
                accessPolicy.context,
                accessPolicy.type,
                accessPolicy.id,
                accessPolicy.idsProvider,
                accessPolicy.idsConsumer,
                pAction,
                pTarget
            )
        } else
        throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AccessPolicySchemaV1)
}

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

@CordaSerializable
data class IdsAction(
    val id : List<String>
)

@CordaSerializable
data class Target(
    val id : String,
    val type : String,
    val idsAssetRefinement : AssetRefinement
)

@CordaSerializable
data class AssetRefinement(
    val type: String,
    val idsLeftOperand: String,
    val idsOperator: String,
    val idsRightOperand: String
)