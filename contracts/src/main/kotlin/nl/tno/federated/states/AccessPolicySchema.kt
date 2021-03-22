package nl.tno.federated.states

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.io.Serializable
import java.util.*
import javax.persistence.*

// Family of schemas
object AccessPolicySchema


// first version of the schema
object AccessPolicySchemaV1 : MappedSchema(
    schemaFamily = AccessPolicySchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentAccessPolicy::class.java, PersistentTarget::class.java, PersistentAssetRefinement::class.java)) {

    override val migrationResource: String
        get() = "accesspolicy.changelog-master"


    @Entity
    @Table(name = "ASSET_REFINEMENT")
    class PersistentAssetRefinement(
        @Id @Column(name = "asset_refinement_id")
        @Type(type = "uuid-char")
        val id: UUID,
        @Column(name = "type")
        val type: String,
        @Column(name = "left_operand")
        val idsLeft_operand: String,
        @Column(name = "operator")
        val idsOperator: String,
        @Column(name = "right_operand")
        val idsRight_operand: String
    ) {
        // Default constructor required by hibernate.
        constructor() : this(
            UUID.randomUUID(),
            "",
            "",
            "",
            ""
        )
    }

    @Entity
    @Table(name = "TARGET")
    class PersistentTarget(
        @Id @Column(name = "target_id")
        @Type(type = "uuid-char")
        val id: UUID,
        @Column(name = "type")
        val type: String,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumn(name = "asset_refinement_id", referencedColumnName = "asset_refinement_id")
        val idsAssetRefinement: AccessPolicySchemaV1.PersistentAssetRefinement
    ) {
        // Default constructor required by hibernate.
        constructor() : this(
            UUID.randomUUID(),
            "",
            PersistentAssetRefinement()
        )
    }

    @Entity
    @Table(name = "ACCESS_POLICY")
    class PersistentAccessPolicy(
        @Column(name = "access_policy_id")
        @Type(type = "uuid-char")
        val id: UUID,
        @Column(name = "context")
        val context: String,
        @Column(name = "type")
        val type: String,
        @Column(name = "ap_id")
        val accessPolicyId: String,
        @Column(name = "provider")
        val idsProvider: String,
        @Column(name = "consumer")
        val idsConsumer: String,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumn(name = "target_id", referencedColumnName = "target_id")
        val idsTarget: PersistentTarget

    ) : PersistentState(), Serializable {
        // Default constructor required by hibernate.
        constructor() : this(
            UUID.randomUUID(),
            "",
            "",
            "",
            "",
            "",
            PersistentTarget()
        )
    }
}