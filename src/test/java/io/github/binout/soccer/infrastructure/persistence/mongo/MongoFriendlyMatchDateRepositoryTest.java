package io.github.binout.soccer.infrastructure.persistence.mongo;

import io.github.binout.soccer.domain.date.FriendlyMatchDate;
import io.github.binout.soccer.domain.date.MatchDate;
import io.github.binout.soccer.domain.player.Player;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mongolink.test.MongolinkRule;

import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LECTRA
 *
 * @author b.prioux
 */
public class MongoFriendlyMatchDateRepositoryTest {

    @Rule
    public MongolinkRule mongolinkRule =  MongolinkRule.withPackage(getClass().getPackage().getName());

    private MongoFriendlyMatchDateRepository repository;
    private MongoPlayerRepository playerRepository;

    @Before
    public void initRepository() {
        playerRepository = new MongoPlayerRepository(mongolinkRule.getCurrentSession());
        repository = new MongoFriendlyMatchDateRepository(mongolinkRule.getCurrentSession());
    }

    @Test
    public void should_persist_date_without_player() {
        repository.add(MatchDate.newDateForFriendly(2016, Month.APRIL, 1));
        repository.session().flush();

        Optional<FriendlyMatchDate> matchDate = repository.byDate(2016, Month.APRIL, 1);
        assertThat(matchDate).isPresent();
        assertThat(matchDate.get().id()).isNotNull();
        assertThat(matchDate.get().presents().count()).isZero();
    }

    @Test
    public void should_persist_date_with_player() {
        Player benoit = new Player("benoit");
        playerRepository.add(benoit);
        repository.session().flush();

        FriendlyMatchDate date = MatchDate.newDateForFriendly(2016, Month.APRIL, 1);
        date.present(benoit);
        repository.add(date);
        repository.session().flush();

        Optional<FriendlyMatchDate> matchDate = repository.byDate(2016, Month.APRIL, 1);
        assertThat(matchDate).isPresent();
        assertThat(matchDate.get().id()).isNotNull();
        assertThat(matchDate.get().presents().count()).isEqualTo(1);
        benoit = matchDate.get().presents().findFirst().get();
        assertThat(benoit.name()).isEqualTo("benoit");
        assertThat(benoit.isPlayerLeague()).isFalse();

        benoit.playsInLeague(true);
        repository.session().flush();

        benoit = repository.byDate(2016, Month.APRIL, 1).get().presents().findFirst().get();
        assertThat(benoit.name()).isEqualTo("benoit");
        assertThat(benoit.isPlayerLeague()).isTrue();
    }
}