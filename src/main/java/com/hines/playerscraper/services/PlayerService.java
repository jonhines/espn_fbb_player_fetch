package com.hines.playerscraper.services;

import com.hines.playerscraper.entities.Player;
import com.hines.playerscraper.entities.Team;
import com.hines.playerscraper.entities.Topics;
import com.hines.playerscraper.models.TeamRoster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class PlayerService extends ESPNService
{

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private RestTemplate restTemplate;

    public PlayerService(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    /**
     * Return a list of all teams in the league with their current rosters
     *
     * @return
     */
    public List<TeamRoster> getTeamRosters(String leagueYear)
    {
        // https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/segments/0/leagues/30710/teams/

        HttpEntity<Topics> entity = new HttpEntity<>(getEspnRequestHeaders(leagueYear, null));
        List<TeamRoster> teams = new ArrayList<>();

        try
        {
            ResponseEntity<List<Team>> teamResponse = restTemplate.exchange(
                "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear + "/segments/0/leagues/" + LEAGUE_ID + "/teams",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Team>>()
                {
                });
            if (teamResponse != null && teamResponse.getBody() != null)
            {
                teamResponse.getBody().stream().forEach(team -> {
                    TeamRoster teamRoster = new TeamRoster();
                    teamRoster.setName(team.getLocation() + " " + team.getNickname());

                    // get teams roster
                    // https://fantasy.espn.com/apis/v3/games/flb/seasons/leagueYear/segments/0/leagues/30710/teams/2?view=roster
                    ResponseEntity<Team> rosterResponse = restTemplate.exchange(
                        "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear + "/segments/0/leagues/" + LEAGUE_ID + "/teams/" + team
                            .getId() + "?view=roster",
                        HttpMethod.GET,
                        entity,
                        Team.class);
                    Set<String> rosterNames = new HashSet<String>();
                    if (rosterResponse != null)
                    {
                        if (rosterResponse.getBody() != null && rosterResponse.getBody().getRoster() != null
                            && !isEmpty(rosterResponse.getBody().getRoster().getEntries()))
                        {
                            rosterResponse.getBody().getRoster().getEntries().stream().forEach(entry ->
                            {
                                if (entry.getPlayerPoolEntry() != null)
                                {
                                    rosterNames.add(entry.getPlayerPoolEntry().getPlayer().getFullName());
                                }
                            });
                        }
                    }
                    teamRoster.setRoster(rosterNames);
                    teams.add(teamRoster);
                });
            }
            return teams;
        } catch (Exception e)
        {
            logger.error("an error occurred feching team rosters", e);
        }

        return null;
    }

    @Async
    public CompletableFuture<Player> getPlayerByIdAsync(String leagueYear, String playerId)
    {
        return CompletableFuture.completedFuture(getPlayerById(leagueYear, playerId));
    }

    public Player getPlayerById(String leagueYear, String playerId)
    {
        String url = String
            .format("https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear + "/players/%s?scoringPeriodId=0&view=players_wl",
                playerId);

        HttpEntity<Topics> entity = new HttpEntity<>(getEspnRequestHeaders(leagueYear, null));
        try
        {

            ResponseEntity<Player> playerResponseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Player.class);

            return playerResponseEntity.getBody();

        } catch (Exception e)
        {
            logger.error("An error occurred fetching player details for Player ID: {}", playerId, e);
        }

        return null;
    }


}
