package io.github.ermadmi78.kobby.cinema.server.resolvers

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.dto.*
import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.resolver.CinemaMutationResolver
import io.github.ermadmi78.kobby.cinema.server.eventbus.EventBus
import io.github.ermadmi78.kobby.cinema.server.jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component

/**
 * Created on 03.03.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */
@Component
class MutationResolver(
    private val dslContext: DSLContext,
    private val eventBus: EventBus
) : CinemaMutationResolver {
    override suspend fun createCountry(name: String): CountryDto {
        val newCountry = dslContext.insertInto(COUNTRY)
            .set(COUNTRY.NAME, name)
            .returning()
            .fetchOne()!!
            .toDto()

        eventBus.fireCountryCreated(newCountry)
        return newCountry
    }

    override suspend fun createFilm(
        countryId: Long,
        film: FilmInput,
        tags: TagInput?
    ): FilmDto {
        val newFilm = dslContext.insertInto(FILM)
            .set(FILM.COUNTRY_ID, countryId)
            .set(FILM.TITLE, film.title)
            .set(FILM.GENRE, film.genre.toRecord())
            .set(FILM.TAGS, if (tags == null) arrayOf<Any>() else arrayOf<Any>(tags.value))
            .returning()
            .fetchOne()!!
            .toDto()

        eventBus.fireFilmCreated(newFilm)
        return newFilm
    }

    override suspend fun createActor(
        countryId: Long,
        actor: ActorInput,
        tags: TagInput?
    ): ActorDto {
        val newActor = dslContext.insertInto(ACTOR)
            .set(ACTOR.COUNTRY_ID, countryId)
            .set(ACTOR.FIRST_NAME, actor.firstName)
            .set(ACTOR.LAST_NAME, actor.lastName)
            .set(ACTOR.BIRTHDAY, actor.birthday)
            .set(ACTOR.GENDER, actor.gender.toRecord())
            .set(ACTOR.TAGS, if (tags == null) arrayOf<Any>() else arrayOf<Any>(tags.value))
            .returning()
            .fetchOne()!!
            .toDto()

        eventBus.fireActorCreated(newActor)
        return newActor
    }

    override suspend fun associate(
        filmId: Long,
        actorId: Long
    ): Boolean = dslContext
        .insertInto(FILM_ACTOR)
        .set(FILM_ACTOR.FILM_ID, filmId)
        .set(FILM_ACTOR.ACTOR_ID, actorId)
        .onDuplicateKeyIgnore()
        .execute() == 1

    override suspend fun tagFilm(
        filmId: Long,
        tagValue: String
    ): Boolean = dslContext.update(FILM)
        .set(FILM.TAGS, DSL.function("ARRAY_APPEND", FILM.TAGS.dataType, FILM.TAGS, DSL.`val`(tagValue)))
        .where(FILM.ID.eq(filmId))
        .and(filmTagsContains(tagValue).not())
        .execute() == 1

    override suspend fun tagActor(
        actorId: Long,
        tagValue: String
    ): Boolean = dslContext
        .update(ACTOR)
        .set(ACTOR.TAGS, DSL.function("ARRAY_APPEND", ACTOR.TAGS.dataType, ACTOR.TAGS, DSL.`val`(tagValue)))
        .where(ACTOR.ID.eq(actorId))
        .and(actorTagsContains(tagValue).not())
        .execute() == 1
}