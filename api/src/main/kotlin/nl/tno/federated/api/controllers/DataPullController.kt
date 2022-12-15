package nl.tno.federated.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.identity.CordaX500Name
import nl.tno.federated.api.corda.CordaNodeService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


/**
 * Create and query events.
 */
@RestController
@RequestMapping("/datapull")
@Api(value = "DataPullController", tags = ["Data pull endpoints"])
class DataPullController(private val cordaNodeService: CordaNodeService) {

    private val log = LoggerFactory.getLogger(DataPullController::class.java)

    @ApiOperation(value = "Request data and run a SPARQL query on another unknown node")
    @GetMapping(value = ["/request/{eventuuid}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun request(@RequestBody query: String, @PathVariable eventuuid: String): ResponseEntity<String> {
        val counterPartyCertificate = cordaNodeService.extractSender(eventuuid)
        return request(query, counterPartyCertificate.name.organisation, counterPartyCertificate.name.locality, counterPartyCertificate.name.country)
    }

    @ApiOperation(value = "Request data and run a SPARQL query on another node")
    @PostMapping(value = ["/request/{destinationOrganisation}/{destinationLocality}/{destinationCountry}"])
    private fun request(
        @RequestBody query: String,
        @PathVariable destinationOrganisation: String?,
        @PathVariable destinationLocality: String?,
        @PathVariable destinationCountry: String?
    ): ResponseEntity<String> {
        if (destinationOrganisation == null || destinationLocality == null || destinationCountry == null) {
            return ResponseEntity("Missing destination fields", HttpStatus.BAD_REQUEST)
        }

        log.info("Data pull SPARQL query requested for destination: {}", destinationOrganisation)
        val uuidOfStateWithResult = cordaNodeService.startDataPullFlow(query, CordaX500Name(destinationOrganisation, destinationLocality, destinationCountry))

        log.info("Data pull completed for destination: {}, result can be found in DataPullState with UUID: {}", destinationOrganisation, uuidOfStateWithResult)
        return ResponseEntity("State with result: $uuidOfStateWithResult", HttpStatus.ACCEPTED)
    }

    @ApiOperation(value = "Retrieve data from a previous request")
    @GetMapping(value = ["/retrieve/{uuid}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun retrieve(@PathVariable uuid: String): ResponseEntity<List<String>> {
        val datapullResults = cordaNodeService.getDataPullResults(uuid)
        return ResponseEntity(datapullResults, HttpStatus.OK)
    }

}