package io.github.ermadmi78.kobby.cinema.server

import graphql.kickstart.tools.CoroutineContextProvider
import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import io.github.ermadmi78.kobby.cinema.server.resolvers.QueryResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

@Configuration
@EnableWebFlux
class ApplicationConfiguration {
    /**
     * Static http resources for GraphIQL (http://localhost:8080/graphiql)
     */
    @Bean
    fun resourcesRouter() = resources("/**", ClassPathResource("static/"))

    /**
     * scalar JSON support
     */
    @Bean
    fun scalarJson(): GraphQLScalarType = ExtendedScalars.Json

    /**
     * scalar Date support
     */
    @Bean
    fun scalarDate(): GraphQLScalarType = ExtendedScalars.Date

    /**
     * Coroutine based resolvers dispatcher
     * @see: [QueryResolver.country]
     */
    @Bean
    fun resolverDispatcher(): CoroutineDispatcher = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
        BasicThreadFactory.Builder()
            .namingPattern("resolver-thread-%d")
            .daemon(true)
            .build()
    ).asCoroutineDispatcher()

    /**
     * Coroutine dispatcher configuration
     */
    @Bean
    fun coroutineContextProvider(resolverDispatcher: CoroutineDispatcher): CoroutineContextProvider =
        object : CoroutineContextProvider {
            override fun provide(): CoroutineContext {
                return resolverDispatcher
            }
        }
}
