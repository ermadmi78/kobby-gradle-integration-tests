package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.ActorDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.CountryDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.FilmDto
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.Genre
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaActorResolver
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL.trueCondition
import org.springframework.stereotype.Component

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class ActorResolver(private val dslContext: DSLContext) : CinemaActorResolver {
    companion object {
        private val ALL_FIELDS = setOf("id", "firstName", "lastName", "birthday", "gender")
    }

    override suspend fun fields(
        actor: ActorDto,
        keys: List<String>?
    ): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        (keys?.toSet() ?: ALL_FIELDS).forEach {
            when (it) {
                "id" -> result[it] = actor.id
                "firstName" -> result[it] = actor.firstName
                "lastName" -> result[it] = actor.lastName
                "birthday" -> result[it] = actor.birthday
                "gender" -> result[it] = actor.gender
            }
        }

        return result
    }

    override suspend fun country(actor: ActorDto): CountryDto = dslContext
        .selectFrom(COUNTRY)
        .where(COUNTRY.ID.eq(actor.countryId))
        .fetchAny { it.toDto() }!!

    override suspend fun films(
        actor: ActorDto,
        title: String?,
        genre: Genre?,
        limit: Int,
        offset: Int
    ): List<FilmDto> = dslContext
        .select(FILM.asterisk())
        .from(FILM)
        .join(FILM_ACTOR)
        .onKey()
        .where(FILM_ACTOR.ACTOR_ID.eq(actor.id))
        .and(trueCondition().andFilms(title, genre))
        .limit(offset.prepare(), limit.prepare())
        .fetch {
            it.into(FILM).toDto()
        }
}