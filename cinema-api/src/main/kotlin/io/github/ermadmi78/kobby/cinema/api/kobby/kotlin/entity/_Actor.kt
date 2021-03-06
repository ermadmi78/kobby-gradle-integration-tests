package io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.entity

import io.github.ermadmi78.kobby.cinema.api.kobby.kotlin.fetchActor

/**
 * Created on 21.05.2021
 *
 * @author Dmitry Ermakov (ermadmi78@gmail.com)
 */

suspend fun Actor.refresh(id: Long, __projection: (ActorProjection.() -> Unit)? = null): Actor =
    fetchActor(id) {
        __projection?.invoke(this) ?: __withCurrentProjection()
    }

suspend fun Actor.addFilm(filmId: Long): Boolean = mutation {
    associate(filmId, id)
}.associate

suspend fun Actor.tag(tagValue: String): Boolean = mutation {
    tagActor(id, tagValue)
}.tagActor