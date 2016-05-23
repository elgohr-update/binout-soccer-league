package io.github.binout.soccer.domain.match;

import io.github.binout.soccer.domain.date.FriendlyMatchDate;
import io.github.binout.soccer.domain.date.LeagueMatchDate;
import io.github.binout.soccer.domain.player.Player;
import io.github.binout.soccer.domain.player.PlayerRepository;
import io.github.binout.soccer.domain.season.SeasonStatistics;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class MatchService {

    @Inject
    PlayerRepository playerRepository;

    public LeagueMatch planLeagueMatch(LeagueMatchDate date, SeasonStatistics statistics) {
        Map<Integer, List<Player>> playersByGamesPlayed = playerRepository.all()
                .filter(date::isPresent)
                .filter(Player::isPlayerLeague)
                .collect(Collectors.groupingBy(statistics::gamesPlayed));
        TreeMap<Integer, List<Player>> treeMap = new TreeMap<>(playersByGamesPlayed);
        return new LeagueMatch(date, extractPlayers(treeMap, LeagueMatch.MAX_PLAYERS));
    }

    public FriendlyMatch planFriendlyMatch(FriendlyMatchDate date, SeasonStatistics statistics) {
        Map<Integer, List<Player>> playersByGamesPlayed = playerRepository.all()
                .filter(date::isPresent)
                .collect(Collectors.groupingBy(statistics::gamesPlayed));
        TreeMap<Integer, List<Player>> treeMap = new TreeMap<>(playersByGamesPlayed);

        return new FriendlyMatch(date, extractPlayers(treeMap, FriendlyMatch.MAX_PLAYERS));
    }

    private Set<Player> extractPlayers(TreeMap<Integer, List<Player>> treeMap, int maxPlayers) {
        Set<Player> players = new HashSet<>();
        Iterator<Map.Entry<Integer, List<Player>>> iterator = treeMap.entrySet().iterator();
        while (iterator.hasNext() && teamIsNotFull(players, maxPlayers)) {
            Map.Entry<Integer, List<Player>> entry = iterator.next();
            Iterator<Player> playerIterator = entry.getValue().iterator();
            while (playerIterator.hasNext() && teamIsNotFull(players, maxPlayers)) {
                players.add(playerIterator.next());
            }
        }
        return players;
    }

    private boolean teamIsNotFull(Set<Player> players, int maxPlayers) {
        return players.size() < maxPlayers;
    }
}