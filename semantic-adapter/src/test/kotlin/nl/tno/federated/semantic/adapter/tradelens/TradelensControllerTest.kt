package nl.tno.federated.semantic.adapter.tradelens

import nl.tno.federated.semantic.adapter.core.TripleService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.file.Files

@WebMvcTest(controllers = [TradelensController::class, TripleService::class, TradelensMapper::class])
class TradelensControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun tradelensMultipleEventsTest() {
        this.mockMvc.perform(
            post("/tradelens/events")
                .param("base_uri", "https://ontology.tno.nl/example")
                .content("tl-events-wrapper-node-input.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }

    @Test
    fun tradelensContainerDetailsTest() {
        this.mockMvc.perform(
            post("/tradelens/containers")
                .param("base_uri", "https://ontology.tno.nl/example")
                .content("tl-container-details.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
    }
}

private fun String.readAllBytes() = Files.readAllBytes(ClassPathResource(this).file.toPath())