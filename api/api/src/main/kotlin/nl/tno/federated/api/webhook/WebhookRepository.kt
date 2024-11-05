package nl.tno.federated.api.webhook

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WebhookRepository : CrudRepository<WebhookEntity, Long> {
    fun findByClientId(clientId: String): List<WebhookEntity>
}