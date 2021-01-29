package nl.tno.federated.states

import com.sun.org.apache.xpath.internal.operations.Bool
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********
//@BelongsToContract(DigitalTwinContract::class)
data class CargoState(
    override val dangerous: Bool,
    override val dryBulk: Bool,
    override val excise: Bool,
    override val liquidBulk: Bool,
    override val maximumSize: Int,
    override val maximumTemperature: String,
    override val maximumVolume: Int,
    override val minimumSize: Int,
    override val minimumTemperature: String,
    override val minimumVolume: Int,
    override val minimumWeight: Float,
    override val natureOfCargo: String,
    override val numberOfTEU: Int,
    override val properties: String,
    override val reefer: Bool,
    override val tarWeight: Float,
    override val temperature: String,
    override val type: String,
    override val waste: Bool,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()) :
        LinearState,
        Cargo(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )

open class Cargo(
    open val dangerous : Bool,
    open val dryBulk : Bool,
    open val excise : Bool,
    open val liquidBulk : Bool,
    open val maximumSize : Int,
    open val maximumTemperature : String,
    open val maximumVolume : Int,
    open val minimumSize : Int,
    open val minimumTemperature : String,
    open val minimumVolume :  Int,
    open val minimumWeight : Float,
    open val natureOfCargo : String,
    open val numberOfTEU : Int,
    open val properties : String,
    open val reefer : Bool,
    open val tarWeight : Float,
    open val temperature: String,
    open val type : String, // not actually a String, more info needed
    open val waste : Bool
) : PhysicalObject()
