package io.github.ermadmi78.kobby.cinema.kotlin.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.CinemaMapper
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.adapter.ktor.CinemaCompositeKtorAdapter
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.cinemaContextOf
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.reflect.KClass

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@SpringBootApplication
class Application : CommandLineRunner {
    override fun run(vararg args: String?): Unit = runBlocking {
        val client = HttpClient {
            install(WebSockets)
        }

        val mapper = jacksonObjectMapper()
            .registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(JavaTimeModule())
            // Force Jackson to serialize dates as String
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        val adapter = CinemaCompositeKtorAdapter(
            client,
            "http://localhost:8080/graphql",
            "ws://localhost:8080/subscriptions",
            object : CinemaMapper {
                override fun serialize(value: Any): String =
                    mapper.writeValueAsString(value)

                override fun <T : Any> deserialize(content: String, contentType: KClass<T>): T =
                    mapper.readValue(content, contentType.java)
            }
        ) {
            println(">> ${it.query}")
            println(">> ${it.variables}")
        }
        val context = cinemaContextOf(adapter)

        //**************************************************************************************************************

        val films = context.query {
            films {
                id()
                title()
                genre()
            }
        }.films

        for (film in films) {
            println("Film id=${film.id} title=${film.title} genre=${film.genre}")
        }

        //**************************************************************************************************************

        context.subscription {
            filmCreated {
                id()
                title()
                genre()
            }
        }.subscribe {
            while (true) {
                val newFilm = receive().filmCreated
                println(
                    "New film id=${newFilm.id} " +
                            "title=${newFilm.title} " +
                            "genre=${newFilm.genre}"
                )
            }
        }
    }
}