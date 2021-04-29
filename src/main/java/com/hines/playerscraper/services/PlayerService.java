package com.hines.playerscraper.services;

import com.hines.playerscraper.entities.*;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.ZoneId.*;
import static java.util.Comparator.*;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class PlayerService extends ESPNService
{

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private RestTemplate restTemplate;
    private EmailSenderService emailSenderService;

    public PlayerService(RestTemplate restTemplate, EmailSenderService emailSenderService)
    {
        this.restTemplate = restTemplate;
        this.emailSenderService = emailSenderService;
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
                "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear
                    + "/segments/0/leagues/" + LEAGUE_ID + "/teams",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Team>>()
                {
                });
            if (teamResponse != null && teamResponse.getBody() != null)
            {
                teamResponse.getBody().stream().forEach(teamToFetch ->
                {
                    Team team = getTeam(leagueYear, entity, teamToFetch);
                    TeamRoster teamRoster = TeamRoster.builder()
                        .name(teamToFetch.getLocation() + " " + teamToFetch.getNickname()).build();
                    Set<String> rosterNames = new HashSet<String>();
                    if (team != null)
                    {
                        if (team != null
                            && team.getRoster() != null
                            && !isEmpty(team.getRoster().getEntries()))
                        {
                            team.getRoster().getEntries().stream()
                                .forEach(entry ->
                                {
                                    if (entry.getPlayerPoolEntry() != null)
                                    {
                                        rosterNames.add(
                                            entry.getPlayerPoolEntry().getPlayer().getFullName());
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
            logger.error("an error occurred fetching team rosters", e);
        }

        return null;
    }

    private Team getTeam(String leagueYear, HttpEntity<Topics> entity, Team team)
    {
        // get teams roster
        ResponseEntity<Team> rosterResponse = restTemplate.exchange(
            "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear
                + "/segments/0/leagues/" + LEAGUE_ID + "/teams/" + team
                .getId() + "?view=mRoster",
            HttpMethod.GET,
            entity,
            Team.class);

        return rosterResponse.getBody();
    }

    @Async
    public CompletableFuture<Player> getPlayerByIdAsync(String leagueYear, String playerId)
    {
        return CompletableFuture.completedFuture(getPlayerById(leagueYear, playerId));
    }

    public Player getPlayerById(String leagueYear, String playerId)
    {
        String url = String
            .format("https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear
                    + "/players/%s?scoringPeriodId=0&view=players_wl",
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
            logger
                .error("An error occurred fetching player details for Player ID: {}", playerId, e);
        }

        return null;
    }


    /**
     * Fetch all games for today, then match up those games with each player of the specified team
     * to identify opposing teams and probable pitchers.
     * <p>
     * Take that summarization of data and send an email to the owner.
     *
     * @param leagueYear
     * @param teamId
     * @return
     */
    public Object sendMatchupSummaryForTeamForToday(String leagueYear, int teamId)
    {
        LocalDate d = LocalDate.now(ZoneId.of("America/New_York"));
        String dateToFetchSummaryFor = d.format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        HttpEntity<Topics> entity = new HttpEntity<>(getEspnRequestHeaders(leagueYear, null));

        Team teamToFetch = Team.builder().id(teamId).build();

        Team teamRoster = getTeam(leagueYear, entity, teamToFetch);

        List<Player> myTeamPlayers = new ArrayList<>();
        teamRoster.getRoster().getEntries().stream().forEach(rosterEntry ->
        {
            // only include non-pitchers AKA HITTERS!
            int startingPitcherPositionId = 1;
            int reliefPitcherPositionId = 11;
            Player player = rosterEntry.getPlayerPoolEntry().getPlayer();
            if (player.getDefaultPositionId()
                != startingPitcherPositionId
                && player.getDefaultPositionId()
                != reliefPitcherPositionId)
            {
                // for some reason ESPN loves to hide the handedness of the players, so we have to make
                // another request to get that for each player
                PlayerAthlete playerWithDetails = getPlayerDetails(rosterEntry.getPlayerId());
                player.setDisplayBatsThrows(
                    getHitHandedness(playerWithDetails.getDisplayBatsThrows()));

                myTeamPlayers.add(player);
            }
        });

        // sort players by their default positions
        myTeamPlayers.sort(comparingInt(Player::getDefaultPositionId));

        // get all games for the day:
        String scheduleUrl =
            "https://site.api.espn.com/apis/site/v2/sports/baseball/mlb/scoreboard?dates="
                + dateToFetchSummaryFor;
        ResponseEntity<ScheduledMatchupContainer> scheduledMatchupContainerResponse = restTemplate
            .getForEntity(
                scheduleUrl,
                ScheduledMatchupContainer.class);

        HashMap<String, List<CompetitorsItem>> gameToTeamsInMatchup = new HashMap<>();

        ScheduledMatchupContainer scheduledMatchupContainer = scheduledMatchupContainerResponse
            .getBody();
        scheduledMatchupContainer.getEvents().stream().forEach(event ->
        {

            // always assume theres at least one competition, which is the actual game
            CompetitionsItem theGame = event.getCompetitions().get(0);

            gameToTeamsInMatchup.put(theGame.getId(), theGame.getCompetitors());

            // always assume theres two teams playing
            CompetitorsItem teamOne = theGame.getCompetitors().get(0);
            CompetitorsItem teamTwo = theGame.getCompetitors().get(1);

            // check if any players match for teamOne, if so, write opposing data of teamId two
            Set<Player> myHittersOnTeamOne = myTeamPlayers.stream()
                .filter(player -> String.valueOf(player.getProTeamId()).equals(teamOne.getId()))
                .collect(
                    Collectors.toSet());

            Set<Player> myHittersOnTeamTwo = myTeamPlayers.stream()
                .filter(player -> String.valueOf(player.getProTeamId()).equals(teamTwo.getId()))
                .collect(
                    Collectors.toSet());

            updatePlayersWithOpposingTeamInfo(teamTwo, myHittersOnTeamOne, event);
            updatePlayersWithOpposingTeamInfo(teamOne, myHittersOnTeamTwo, event);

        });

        emailSenderService.sendEmail(myTeamPlayers, "jonrhines@gmail.com");
        return myTeamPlayers;
    }

    /**
     * Add opposing pitcher and team info to each player on the team that they are facing
     *
     * @param opposingTeam
     * @param myPlayersPlayingThatTeam
     * @param gameEvent
     */
    private void updatePlayersWithOpposingTeamInfo(CompetitorsItem opposingTeam,
        Set<Player> myPlayersPlayingThatTeam, EventsItem gameEvent)
    {
        Set<PlayerAthlete> cachedProbablePitchers = new HashSet<>();
        myPlayersPlayingThatTeam.stream().forEach(myPlayer ->
        {

            int probablePitcherId = opposingTeam.getProbables().get(0).getPlayerId();
            Optional<PlayerAthlete> playerAthleteOptional = cachedProbablePitchers.stream()
                .filter(pp -> pp.getId().equals(String.valueOf(probablePitcherId))).findFirst();

            PlayerAthlete playerAthlete = null;
            // if we havent already looked this one up, do so
            if (!playerAthleteOptional.isPresent())
            {
                playerAthlete = getPlayerDetails(probablePitcherId);

                cachedProbablePitchers.add(playerAthlete);
            } else
            {
                playerAthlete = playerAthleteOptional.get();
            }

            playerAthlete.setDisplayBatsThrows(getThrowArm(playerAthlete.getDisplayBatsThrows()));

            myPlayer.setOpposingPitcher(playerAthlete);
            myPlayer.setOpposingTeamHomeAway(opposingTeam.getHomeAway());
            myPlayer.setOpposingTeamId(opposingTeam.getId());
            myPlayer.setPositionName(getPositionName(myPlayer.getDefaultPositionId()));

            String opposingTeamName = opposingTeam.getTeam().getAbbreviation();
            if (opposingTeam.getHomeAway().equals("home"))
            {
                opposingTeamName = "@" + opposingTeamName;
            }
            myPlayer.setOpposingTeamName(opposingTeamName);

            ZonedDateTime dateTimeOfGame = ZonedDateTime.parse(gameEvent.getDate());
            dateTimeOfGame.toLocalDateTime();

            // convert to EST for gameTime
            myPlayer.setGameTime(dateTimeOfGame.withZoneSameInstant(of("America/New_York")).format(
                DateTimeFormatter.ofPattern("hh:mm a")));
        });
    }

    private PlayerAthlete getPlayerDetails(int playerId)
    {
        PlayerAthlete playerAthlete;
        String playerLookupUrl =
            "https://site.api.espn.com/apis/common/v3/sports/baseball/mlb/athletes/"
                + playerId;
        ResponseEntity<PlayerAthleteContainer> athleteResponse = restTemplate.getForEntity(
            playerLookupUrl,
            PlayerAthleteContainer.class);
        playerAthlete = athleteResponse.getBody().getAthlete();
        return playerAthlete;
    }

    /**
     * Get position name of baseball position num
     *
     * @param positionId
     * @return
     */
    public String getPositionName(int positionId)
    {
        if (positionId == 2)
        {
            return "C";
        }
        if (positionId == 3)
        {
            return "1B";
        }
        if (positionId == 4)
        {
            return "2B";
        }
        if (positionId == 5)
        {
            return "3B";
        }
        if (positionId == 6)
        {
            return "SS";
        }
        if (positionId == 7 || positionId == 8 || positionId == 9)
        {
            return "OF";
        }
        return "UTIL";
    }


    /**
     * Assumes displayBatsThrows is a slash delimited value where the first value is hit, second is
     * throw: Right/Right or Left/Right
     *
     * @param displayBatsThrows
     * @return R or L
     */
    private String getThrowArm(String displayBatsThrows)
    {
        // sometimes players dont have the delimited value
        if (displayBatsThrows == null || displayBatsThrows.length() == 1)
        {
            return displayBatsThrows;
        }
        String[] split = displayBatsThrows.split("/");
        String val = split[1];
        return String.valueOf(val.charAt(0));
    }

    /**
     * Assumes displayBatsThrows is a slash delimited value where the first value is hit, second is
     * throw: Right/Right or Left/Right
     *
     * @param displayBatsThrows
     * @return R or L
     */
    private String getHitHandedness(String displayBatsThrows)
    {
        // sometimes players dont have the delimited value
        if (displayBatsThrows == null || displayBatsThrows.length() == 1)
        {
            return displayBatsThrows;
        }
        String[] split = displayBatsThrows.split("/");
        String val = split[0];
        return String.valueOf(val.charAt(0));
    }
}
