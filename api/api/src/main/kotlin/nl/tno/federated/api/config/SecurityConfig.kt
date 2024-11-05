package nl.tno.federated.api.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableWebSecurity
@Configuration
class SecurityConfig {

    @Autowired
    private lateinit var environment: Environment

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @ConditionalOnProperty(prefix = "federated.node.api.security", name = ["enabled"], havingValue = "false")
    @Throws(Exception::class)
    fun securityFilterChainAllAccess(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf {
                it.disable()
            }.cors {
                it.disable()
            }
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }
        return http.build()
    }

    @Bean
    @ConditionalOnProperty(prefix = "federated.node.api.security", name = ["enabled"], havingValue = "true")
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf {
                it.disable()
            }
            .cors {
                it.disable()
            }
            .httpBasic {
                it.realmName("API login")
            }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/*").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/**").permitAll()
                   // .requestMatchers("/api/**").hasRole("API_USER")
                    .requestMatchers("/api/distribution-rules/**").hasRole("API_USER")
                    .requestMatchers("/api/corda/**").hasRole("API_USER")
                    .requestMatchers("/api/event-types/**").hasRole("API_USER")
                    .requestMatchers("/api/events/**").hasRole("API_USER")
                    .requestMatchers("/api/sparql/**").hasRole("API_USER")
                    .requestMatchers("/api/echo/**").hasRole("API_USER")
                    .requestMatchers("/api/webhooks/**").hasRole("WEBHOOK_USER")
                    .anyRequest().authenticated()
            }
        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user: UserDetails =
            User.withUsername(environment.getProperty("federated.node.api.security.api.username"))
                .password(environment.getProperty("federated.node.api.security.api.password"))
                .roles("API_USER")
                .build()

        val webhookUser: UserDetails =
            User.withUsername(environment.getProperty("federated.node.api.security.webhook.username"))
                .password(environment.getProperty("federated.node.api.security.webhook.password"))
                .roles("WEBHOOK_USER")
                .build()

        return InMemoryUserDetailsManager(user,webhookUser)
    }

    @Bean
    @ConditionalOnProperty(prefix = "federated.node.cors", name = ["enabled"], havingValue = "true")
    fun corsConfigurer(environment: Environment): WebMvcConfigurer? {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val allowedOrigins = environment.getProperty("federated.node.cors.allowed-origins")
                if (!allowedOrigins.isNullOrEmpty()) {
                    registry.addMapping("/**").allowedOrigins(*allowedOrigins.split(",").toTypedArray())
                }
            }
        }
    }
}