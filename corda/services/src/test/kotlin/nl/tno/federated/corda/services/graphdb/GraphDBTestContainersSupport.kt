package nl.tno.federated.corda.services.graphdb

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.MountableFile
import java.time.Duration

/**
 * Implementing the TestContainers singleton pattern.
 * Containers will reused between tests and destroyed by a JVM shutdown hook.
 */
abstract class GraphDBTestContainersSupport {

    class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

    companion object {
        var graphDBService: GraphDBService

        @JvmField
        val graphDB: KGenericContainer = KGenericContainer("khaller/graphdb-free:10.0.0")
            .withCommand("-s")
            .withExposedPorts(7200)
            .withAccessToHost(true)
            .withCopyToContainer(MountableFile.forClasspathResource("repositories"), "/repository.init")
            .waitingFor(
                Wait.forLogMessage(".*Started GraphDB in server only mode at port.*", 1)
                    .withStartupTimeout(Duration.ofMinutes(5))
            )
            .apply { portBindings.add("7200:7200") }

        init {
            graphDB.start()

            // Override database.properties with docker properties
            System.setProperty("triplestore.host", graphDB.host)
            System.setProperty("triplestore.port", graphDB.exposedPorts?.firstOrNull()?.toString() ?: "7200")

            graphDBService = GraphDBService()
        }
    }
}