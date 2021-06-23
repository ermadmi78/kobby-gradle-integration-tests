package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Gender
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaFilmResolver
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.trueCondition
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class FilmResolver(private val dslContext: DSLContext) : CinemaFilmResolver {
    companion object {
        private val ALL_FIELDS = setOf("id", "title", "genre")
    }

    override suspend fun fields(
        film: FilmDto,
        keys: List<String>?
    ): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> result[it] = film.id
                "title" -> result[it] = film.title
                "genre" -> result[it] = film.genre
            }
        }

        return result
    }

    override suspend fun country(film: FilmDto): CountryDto = dslContext
        .selectFrom(COUNTRY)
        .where(COUNTRY.ID.eq(film.countryId))
        .fetchAny { it.toDto() }!!

    override suspend fun actors(
        film: FilmDto,
        firstName: String?,
        lastName: String?,
        birthdayFrom: LocalDate?,
        birthdayTo: LocalDate?,
        gender: Gender?,
        limit: Int,
        offset: Int
    ): List<ActorDto> = dslContext
        .select(ACTOR.asterisk())
        .from(ACTOR)
        .join(FILM_ACTOR)
        .onKey()
        .where(FILM_ACTOR.FILM_ID.eq(film.id))
        .and(trueCondition().andActors(firstName, lastName, birthdayFrom, birthdayTo, gender))
        .limit(offset.prepare(), limit.prepare())
        .fetch {
            it.into(ACTOR).toDto()
        }
}