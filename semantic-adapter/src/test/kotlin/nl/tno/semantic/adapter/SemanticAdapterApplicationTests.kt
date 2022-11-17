package nl.tno.semantic.adapter

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.file.Files

@WebMvcTest(controllers = [SemanticAdapterController::class, TrippleService::class])
class SemanticAdapterApplicationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun preMappingEventTest() {
        this.mockMvc.perform(
            post("/")
                .content("pre-mapping-event.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
    }

    @Test
    fun tradelensSingleEventTest() {
        this.mockMvc.perform(
            post("/tradelens-events")
                .param("base_uri", "https://ontology.tno.nl/logistics/federated/tradelens")
                .content("tradelens-single-event.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
    }

    @Test
    fun tradelensMultipleEventsTest() {
        this.mockMvc.perform(
            post("/tradelens-events")
                .param("base_uri", "https://ontology.tno.nl/example")
                .content("tradelens-multiple-events.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
    }

    @Test
    fun tradelensContainerDetailsTest() {
        this.mockMvc.perform(
            post("/tradelens-containers")
                .param("base_uri", "https://ontology.tno.nl/example")
                .content("tradelens-container-details.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
    }

    @Test
    fun tradelensContainerDetailsWithHsCodeTest() {
        this.mockMvc.perform(
            post("/tradelens-containers")
                .param("base_uri", "https://ontology.tno.nl/example")
                .content("tradelens-container-details-with-hs-code.json".readAllBytes())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
    }

    private fun String.readAllBytes() = Files.readAllBytes(ClassPathResource(this).file.toPath())
}