package com.hines.playerscraper.services;

import com.hines.playerscraper.entities.*;
import com.hines.playerscraper.models.TeamRoster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PlayerService
{
    private final static String LEAGUE_ID = "30710";
    private final static String YEAR = "2019";

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private RestTemplate restTemplate;

    public PlayerService(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    /**
     * Return the names of all players that were added as a FreeAgent or Waiver
     * @return
     */
    public Set<String> getAllFreeAgentAddsForTheSeason()
    {
        Set<String> playersAddedAsFreeAgent = new HashSet<>();

        long startDateInMs = getRequestDate("2019/03/28 00:00:00");
        long endDateInMs = getRequestDate("2019/10/01 00:00:00");

        int messageTypeFreeAgentAdd = 178;
        int messageTypeWaiverAdd = 180;

        String filters = "{\"topics\":{\"filterType\":{\"value\":[\"ACTIVITY_TRANSACTIONS\"]},\"limit\":2000,\"limitPerMessageSet\":{\"value\":2000},\"offset\":0,\"sortMessageDate\":{\"sortPriority\":1,\"sortAsc\":false},\"sortFor\":{\"sortPriority\":2,\"sortAsc\":false},\"filterDateRange\":{\"value\":" + startDateInMs + ",\"additionalValue\":" + endDateInMs + "},\"filterIncludeMessageTypeIds\":{\"value\":[" + messageTypeFreeAgentAdd + "," + messageTypeWaiverAdd + "]}}}";

        HttpHeaders headers = getEspnRequestHeaders(filters);

        try
        {
            HttpEntity<Topics> entity = new HttpEntity<>(headers);

            ResponseEntity<Topics> responseEntity = restTemplate.exchange(
                    "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + YEAR + "/segments/0/leagues/" + LEAGUE_ID + "/communication/?view=kona_league_communication",
                    HttpMethod.GET,
                    entity,
                    Topics.class);

            Topics body = responseEntity.getBody();
            // https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/players/%s?scoringPeriodId=0&view=players_wl
            body.getTopics().stream().forEach(topic ->
            {

                // when polling only for FA Adds, there will only be a single message, make this more dynamic later
                Message message = topic.getMessages().get(0);
                Player player = getPlayerById(message.getTargetId());
                if (player != null)
                {
                    playersAddedAsFreeAgent.add(player.getFullName());
                }
            });

            return playersAddedAsFreeAgent;

        } catch (Exception e)
        {
            logger.error("An error occurred fetching all FA adds for the league.", e);
            throw new RuntimeException("An error occurred fetching information from ESPN API! " + e.getMessage());
        }
    }

    /**
     * Return a list of all teams in the league with their current rosters
     * @return
     */
    public List<TeamRoster> getTeamRosters()
    {
        // https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/segments/0/leagues/30710/teams/

        HttpEntity<Topics> entity = new HttpEntity<>(getEspnRequestHeaders(null));
        List<TeamRoster> teams = new ArrayList<>();

        try
        {
            ResponseEntity<List<Team>> teamResponse = restTemplate.exchange(
                    "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + YEAR + "/segments/0/leagues/" + LEAGUE_ID + "/teams",
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
                    // https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/segments/0/leagues/30710/teams/2?view=roster
                    ResponseEntity<Team> rosterResponse = restTemplate.exchange(
                            "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + YEAR + "/segments/0/leagues/" + LEAGUE_ID + "/teams/" + team.getId() + "?view=roster",
                            HttpMethod.GET,
                            entity,
                            Team.class);
                    Set<String> rosterNames = new HashSet<String>();
                    if (rosterResponse != null)
                    {
                        rosterResponse.getBody().getRoster().getEntries().stream().forEach(entry -> {
                            if (entry.getPlayerPoolEntry() != null)
                            {
                                rosterNames.add(entry.getPlayerPoolEntry().getPlayer().getFullName());
                            }
                        });
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

    private HttpHeaders getEspnRequestHeaders(String filters)
    {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer",
                "http://fantasy.espn.com/baseball/recentactivity?leagueId=" + LEAGUE_ID + "&endDate=20190729&seasonId=" + YEAR + "&startDate=20190727&teamId=-1&transactionType=2&activityType=2&page=1");
        headers.add("X-Fantasy-Source", "kona");
        headers.add("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        headers.add("X-Fantasy-Platform", "kona-PROD-6b1bde3ecf8dde941512a5c0d02d8fd8a7461f47");
        headers.add("Origin", "http://fantasy.espn.com");
        headers.add("Cache-Control", "no-cache");
        headers.add("Host", "fantasy.espn.com");
        headers.add("Cookie",
                "espn_s2=AEAwiDIR7Un6znzyyrhqnfVQX4wb2tGm292n3kkeDgvuZNNM98L2KTQuXuO5bVyd0qCjrjxX0gk%2B7SWcDuNljIKb0KKntT43UYeNgeerd6ar16B0jlor1wbrX6wBReMQhI1vriQ8uUuh2RP58%2BBOfpdEcTImfla8T8H5L9uDD%2FwPZh%2FJKw8iSAVYuHu579OVXupmxeWBJ3mb%2B9ZxtTVzb%2FZC%2FMNODCTLGOp9U8w1YKoE86p8GgCfhP8qt%2BrTzQqWgD%2B0y2IesAObnSXTTa53YkYhlMta9SV%2BnOoDfjq96XDbJg%3D%3D; SWID={FF4191B2-668A-44DE-951C-6D8C351B60C1}");
        headers.add("Connection", "keep-alive");
        headers.add("cache-control", "no-cache");


        if (filters != null)
        {
            headers.add("X-Fantasy-Filter", filters);
        }

        return headers;
    }

    private Player getPlayerById(String playerId)
    {
        String url = String.format("https://fantasy.espn.com/apis/v3/games/flb/seasons/" + YEAR + "/players/%s?scoringPeriodId=0&view=players_wl",
                playerId);

        HttpEntity<Topics> entity = new HttpEntity<>(getEspnRequestHeaders(null));
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

    /**
     * @param dateString - example: "2019/03/01 00:00:00"
     * @return
     */
    private long getRequestDate(String dateString)
    {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

}
