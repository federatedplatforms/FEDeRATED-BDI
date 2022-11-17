package nl.tno.federated

import org.junit.ClassRule
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

open class GraphDBTestContainersSupport {

    class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

    companion object {

        @ClassRule @JvmField
        val graphDB: KGenericContainer = KGenericContainer("khaller/graphdb-free:10.0.0")
            .withExposedPorts(7200)
            .withAccessToHost(true)
            .waitingFor(
                Wait.forLogMessage(".*Started GraphDB in workbench mode at port.*", 1)
                    .withStartupTimeout(Duration.ofMinutes(1))
            )
            .apply { portBindings.add("7200:7200") }
    }
}