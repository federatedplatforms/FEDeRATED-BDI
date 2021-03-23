package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.AccessPoliciesContract

@BelongsToContract(AccessPoliciesContract::class)
data class AccessPolicyState(
    val context : String,
    val type : String,
    val id : String,
    val idsProvider : String,
    val idsConsumer : String,
    val idsPermission : List<IdsAction>,
    val idsTarget : Target,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is AccessPolicySchemaV1) {

            val pAssetRefinement = AccessPolicySchemaV1.PersistentAssetRefinement(
                    UniqueIdentifier().id,
                    idsTarget.idsAssetRefinement.type,
                    idsTarget.idsAssetRefinement.idsLeftOperand,
                    idsTarget.idsAssetRefinement.idsOperator,
                    idsTarget.idsAssetRefinement.idsRightOperand
                )

            val pTarget = AccessPolicySchemaV1.PersistentTarget(
                    UniqueIdentifier().id,
                    idsTarget.type,
                    pAssetRefinement
                )

            return AccessPolicySchemaV1.PersistentAccessPolicy(
                linearId.id,
                context,
                type,
                id,
                idsProvider,
                idsConsumer,
                pTarget
            )
        } else
        throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(AccessPolicySchemaV1)
}

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