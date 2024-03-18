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
    @ConditionalOnProperty(prefix = "bdi.api.security", name = ["enabled"], havingValue = "false")
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
    @ConditionalOnProperty(prefix = "bdi.api.security", name = ["enabled"], havingValue = "true")
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
                    .requestMatchers("/api/**").hasRole("API_USER")
                    .anyRequest().authenticated()
            }

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user: UserDetails =
            User.withUsername(environment.getProperty("bdi.api.security.username"))
                .password(environment.getProperty("bdi.api.security.password"))
                .roles("API_USER")
                .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    @ConditionalOnProperty(prefix = "bdi.node.cors", name = ["enabled"], havingValue = "true")
    fun corsConfigurer(environment: Environment): WebMvcConfigurer? {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val allowedOrigins = environment.getProperty("bdi.node.cors.allowed-origins")
                if (!allowedOrigins.isNullOrEmpty()) {
                    registry.addMapping("/**").allowedOrigins(*allowedOrigins.split(",").toTypedArray())
                }
            }
        }
    }
}