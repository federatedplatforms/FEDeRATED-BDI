package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import nl.tno.federated.api.event.distribution.EventDistributionRuleConfiguration
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/distribution-rules", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "DistributionRulesController", description = "Returns info regarding the distribution roles for this node.")
class DistributionRulesController(private val rules: EventDistributionRuleConfiguration) {
    @GetMapping()
    fun getDistributionRoles() = rules.getDistributionRules()

}