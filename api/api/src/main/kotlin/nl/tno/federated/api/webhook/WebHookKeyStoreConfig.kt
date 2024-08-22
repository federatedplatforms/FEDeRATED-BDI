package nl.tno.federated.api.webhook

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException

@Configuration
class WebHookKeyStoreConfig {


    @Autowired
    private lateinit var environment: Environment

    @Bean
    @ConditionalOnProperty(prefix = "federated.node.security.keystore", name = ["enabled"], havingValue = "true")
    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class, IOException::class)
    fun privateKey(): Key {
        val keyStore = KeyStore.getInstance(environment.getProperty("federated.node.security.keystore.type"))
        keyStore.load(environment.getProperty("federated.node.security.keystore.location")?.let { ClassPathResource(it).inputStream }, environment.getProperty("federated.node.security.keystore.password")?.toCharArray() ?: "".toCharArray() )
        val privKey = keyStore.getKey(environment.getProperty("federated.node.security.keystore.alias"), environment.getProperty("federated.node.security.keystore.password")?.toCharArray() ?: "".toCharArray())
        return privKey
    }
}