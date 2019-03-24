package io.github.binout.soccer.application

import io.github.binout.soccer.domain.player.Player
import io.github.binout.soccer.domain.player.PlayerRepository
import org.springframework.stereotype.Component

@Component
class GetAllLeaguePlayers(private val playerRepository: PlayerRepository) {

    fun execute(): List<Player> = playerRepository.all().filter { it.isPlayerLeague }
}

@Component
class GetAllPlayers(private val playerRepository: PlayerRepository) {

    fun execute(): List<Player> = playerRepository.all()
}

@Component
class GetPlayer(private val playerRepository: PlayerRepository) {

    fun execute(name: String): Player? = playerRepository.byName(name)
}


@Component
class ReplacePlayer(private val playerRepository: PlayerRepository) {

    fun execute(name: String, email: String?, playerLeague: Boolean?, goalkeeper: Boolean?) {
        val player = playerRepository.byName(name) ?: Player(name = name)
        email?.let { player.email = it }
        playerLeague?.let { player.isPlayerLeague = it }
        goalkeeper?.let { player.isGoalkeeper = it }
        playerRepository.add(player)
    }
}