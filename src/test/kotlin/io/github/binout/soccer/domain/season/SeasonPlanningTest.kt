package io.github.binout.soccer.domain.season

import io.github.binout.soccer.domain.date.MatchDate
import io.github.binout.soccer.domain.player.Player
import io.github.binout.soccer.domain.player.PlayerRepository
import io.github.binout.soccer.infrastructure.persistence.InMemoryFriendlyMatchDateRepository
import io.github.binout.soccer.infrastructure.persistence.InMemoryLeagueMatchDateRepository
import io.github.binout.soccer.infrastructure.persistence.InMemoryPlayerRepository
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import org.springframework.context.ApplicationEventPublisher
import java.time.Month
import java.util.*
import java.util.stream.IntStream

internal class SeasonPlanningTest : WithAssertions {

    private lateinit var seasonPlanning: SeasonPlanning
    private lateinit var matchPlanning: MatchPlanning
    private lateinit var playerRepository: PlayerRepository

    private val DATE_FOR_LEAGUE = MatchDate.newDateForLeague(2016, Month.APRIL, 15)
    private val DATE_FOR_FRIENDLY = MatchDate.newDateForFriendly(2016, Month.APRIL, 15)
    private val EMPTY_SEASON = Season("2016")

    @BeforeEach
    fun init() {
        playerRepository = InMemoryPlayerRepository()

        val leagueMatchDateRepository = InMemoryLeagueMatchDateRepository()
        leagueMatchDateRepository.add(DATE_FOR_LEAGUE)

        val friendlyMatchDateRepository = InMemoryFriendlyMatchDateRepository()
        friendlyMatchDateRepository.add(DATE_FOR_FRIENDLY)

        matchPlanning = MatchPlanning(playerRepository, friendlyMatchDateRepository, leagueMatchDateRepository)
        seasonPlanning = SeasonPlanning(playerRepository, friendlyMatchDateRepository, leagueMatchDateRepository, matchPlanning, Mockito.mock(ApplicationEventPublisher::class.java))
    }

    private fun generatePlayer(nb: Int) {
        IntStream.range(0, nb)
                .mapToObj { _ -> UUID.randomUUID().toString() }
                .map<Player> { Player(it) }
                .forEach { this.addPlayer(it) }
    }

    private fun addPlayer(p: Player) {
        playerRepository.add(p)
        DATE_FOR_LEAGUE.present(p)
        DATE_FOR_FRIENDLY.present(p)
    }

    private fun generateLeaguePlayer(nb: Int) {
        IntStream.range(0, nb)
                .mapToObj { _ -> UUID.randomUUID().toString() }
                .map { p ->
                    val player = Player(p)
                    player.playsInLeague(true)
                    player
                }
                .forEach { this.addPlayer(it) }
    }

    @Test
    fun at_least_5_league_players_for_league_match() {
        generatePlayer(20)
        generateLeaguePlayer(3)

        assertThrows<IllegalArgumentException> { seasonPlanning.planLeagueMatch(EMPTY_SEASON, DATE_FOR_LEAGUE) }
    }

    @ParameterizedTest
    @CsvSource("5, 6, 7")
    fun min_5_max_7_players_for_league_match(nbLeaguePlayer: String) {
        generatePlayer(20)
        generateLeaguePlayer(Integer.parseInt(nbLeaguePlayer))

        val leagueMatch = seasonPlanning.planLeagueMatch(EMPTY_SEASON, DATE_FOR_LEAGUE)

        assertThat(leagueMatch.players().count()).isBetween(5L, 7L)
    }

    @Test
    fun goalkeeper_priority_for_league_match() {
        generateLeaguePlayer(10)

        val goalName = UUID.randomUUID().toString()
        addPlayer(createGoalKeeper(goalName))

        val leagueMatch = seasonPlanning.planLeagueMatch(EMPTY_SEASON, DATE_FOR_LEAGUE)

        assertThat(leagueMatch.players()).contains(goalName)
    }

    private fun createGoalKeeper(goalName: String): Player {
        val goalKeeper = Player(goalName)
        goalKeeper.playsInLeague(true)
        goalKeeper.playsAsGoalkeeper(true)
        return goalKeeper
    }

    @Test
    fun at_least_10_players_for_friendly_match() {
        generatePlayer(7)

        assertThrows<java.lang.IllegalArgumentException> {  seasonPlanning.planFriendlyMatch(EMPTY_SEASON, DATE_FOR_FRIENDLY)}
    }

    @Test
    fun max_10_players_for_friendly_match() {
        generatePlayer(20)

        val friendlyMatch = seasonPlanning.planFriendlyMatch(EMPTY_SEASON, DATE_FOR_FRIENDLY)

        assertThat(friendlyMatch.players().count()).isEqualTo(10)
    }

    @Test
    fun substitutes_if_more_than_10_players() {
        generatePlayer(17)
        val friendlyMatch = seasonPlanning.planFriendlyMatch(EMPTY_SEASON, DATE_FOR_FRIENDLY)

        val substitutes = matchPlanning.getSubstitutes(EMPTY_SEASON, friendlyMatch)

        assertThat(substitutes).hasSize(7)
    }

}