package com.hines.playerscraper.services;

import com.hines.playerscraper.entities.*;
import com.hines.playerscraper.models.TeamRoster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

import static com.hines.playerscraper.entities.Player.getHitHandedness;
import static com.hines.playerscraper.entities.Player.getThrowArm;
import static java.time.ZoneId.of;
import static java.util.Comparator.comparingInt;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class PlayerService extends ESPNService
{

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    private RestTemplate restTemplate;
    private EmailSenderService emailSenderService;

    public PlayerService(@Qualifier("espnTemplate") RestTemplate restTemplate,
        EmailSenderService emailSenderService)
    {
        this.restTemplate = restTemplate;
        this.emailSenderService = emailSenderService;
    }

    public void sendFreeAgentBatsToConsiderEmail(String leagueYear, int numberOfPlayers)
    {
        String filters =
            "{\"players\": {\"filterStatus\": {\"value\": [\"FREEAGENT\"] }, \"filterSlotIds\":{\"value\":[0,1,2,3,4,5,6,7,8,9,10,11,12,19]}, \"limit\": "
                + numberOfPlayers
                + ", \"offset\": 0, \"sortPercOwned\": {\"sortAsc\": false, \"sortPriority\": 1 }, \"sortDraftRanks\": {\"sortPriority\": 100, \"sortAsc\": true, \"value\": \"STANDARD\"}, \"filterRanksForRankTypes\": {\"value\": [\"STANDARD\"] } } }";

        HashSet<Player> players = getAvailablePlayers(leagueYear, filters);

        LocalDate d = LocalDate.now(ZoneId.of("America/New_York"));
        String dateToFetchSummaryFor = d.format(DateTimeFormatter.ofPattern("YYYYMMdd"));
        ScheduledMatchupContainer daysSchedule = getDaysSchedule(dateToFetchSummaryFor);

        identifyMatchupsForPlayers(daysSchedule, players);

        List<Player> playersList = new ArrayList<>(players);

        List<Player> playersWorthPickingUp = playersList.stream()
            .filter(p -> p.getOpposingPitcher() != null && !p.isInjured())
            .collect(Collectors.toList());

        Collections.sort(playersWorthPickingUp, (player1, player2) ->
        {
            Double opposingOps1 = player1.getOPSBasedOnOpposingPitcher();
            Double opposingOps2 = player2.getOPSBasedOnOpposingPitcher();

            return opposingOps1.compareTo(opposingOps2);
        });

        // sort players by OPS against opposing pitcher
        playersWorthPickingUp.stream()
            .sorted(Comparator.comparingDouble(Player::getOPSBasedOnOpposingPitcher));
        // sort by descending OPS
        Collections.reverse(playersWorthPickingUp);

        // send to me only!
        emailSenderService.sendFABatsEmail(playersWorthPickingUp, "jonrhines@gmail.com");

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
     * @param teamIds
     * @return
     */
    public void sendMatchupSummaryForTeamsForToday(String leagueYear, Set<Integer> teamIds)
    {
        LocalDate d = LocalDate.now(ZoneId.of("America/New_York"));
        String dateToFetchSummaryFor = d.format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        HttpEntity<Topics> entity = new HttpEntity<>(getEspnRequestHeaders(leagueYear, null));

        // get all games for the day:
        ScheduledMatchupContainer scheduledMatchupContainer = getDaysSchedule(
            dateToFetchSummaryFor);

        // loop through each team and attempt to send
        teamIds.stream().forEach(teamId ->
        {

            String email = lookupEmailByTeam(teamId);
            // no email setup? then dont bother!
            if (email == null)
            {
                return;
            }

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
                    player.setPlayerId(rosterEntry.getPlayerId());
                    player.setDisplayBatsThrows(
                        getHitHandedness(playerWithDetails.getDisplayBatsThrows()));

                    myTeamPlayers.add(player);
                }
            });

            // sort players by their default positions
            myTeamPlayers.sort(comparingInt(Player::getDefaultPositionId));

            scheduledMatchupContainer.getEvents().stream().forEach(event ->
            {
                // always assume theres at least one competition, which is the actual game
                CompetitionsItem theGame = event.getCompetitions().get(0);

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

            if (email != null)
            {
                emailSenderService.sendMatchupEmail(myTeamPlayers, email);
            }
        });
    }

    private Set<Player> identifyMatchupsForPlayers(ScheduledMatchupContainer scheduleOfGames,
        Set<Player> players)
    {
        scheduleOfGames.getEvents().stream().forEach(event ->
        {
            // always assume theres at least one competition, which is the actual game
            CompetitionsItem theGame = event.getCompetitions().get(0);

            // always assume theres two teams playing
            CompetitorsItem teamOne = theGame.getCompetitors().get(0);
            CompetitorsItem teamTwo = theGame.getCompetitors().get(1);

            // check if any players match for teamOne, if so, write opposing data of teamId two
            Set<Player> myHittersOnTeamOne = players.stream()
                .filter(player -> String.valueOf(player.getProTeamId()).equals(teamOne.getId()))
                .collect(
                    Collectors.toSet());

            Set<Player> myHittersOnTeamTwo = players.stream()
                .filter(player -> String.valueOf(player.getProTeamId()).equals(teamTwo.getId()))
                .collect(
                    Collectors.toSet());

            updatePlayersWithOpposingTeamInfo(teamTwo, myHittersOnTeamOne, event);
            updatePlayersWithOpposingTeamInfo(teamOne, myHittersOnTeamTwo, event);

        });

        return players;
    }


    /**
     * Run a request to fetch the currently available free agent players mapped and hydrated to
     * playerNames
     *
     * @param leagueYear
     * @param filters
     * @return
     */
    private HashSet<Player> getAvailablePlayers(String leagueYear, String filters)
    {
        HashSet<Player> players = new HashSet<>();

        HttpHeaders headers = getEspnRequestHeaders(leagueYear, filters);

        try
        {
            HttpEntity<Topics> entity = new HttpEntity<>(headers);

            String url = "https://fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear
                + "/segments/0/leagues/" + LEAGUE_ID + "?&view=kona_player_info";
            ResponseEntity<FreeAgentContainer> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                FreeAgentContainer.class);

            FreeAgentContainer body = responseEntity.getBody();
            body.getPlayers().parallelStream().forEach(freeAgent ->
            {
                players.add(freeAgent.getPlayer());
            });

            return players;

        } catch (Exception e)
        {
            logger.error("An error occurred fetching all available free agents for the league.", e);
            throw new RuntimeException(
                "An error occurred fetching information from ESPN API! " + e.getMessage());
        }
    }

    private ScheduledMatchupContainer getDaysSchedule(String dateToFetchSummaryFor)
    {
        String scheduleUrl =
            "https://site.api.espn.com/apis/site/v2/sports/baseball/mlb/scoreboard?dates="
                + dateToFetchSummaryFor;
        ResponseEntity<ScheduledMatchupContainer> scheduledMatchupContainerResponse = restTemplate
            .getForEntity(
                scheduleUrl,
                ScheduledMatchupContainer.class);

        ScheduledMatchupContainer scheduledMatchupContainer = scheduledMatchupContainerResponse
            .getBody();
        return scheduledMatchupContainer;
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
        myPlayersPlayingThatTeam.parallelStream().forEach(myPlayer ->
        {

            if (opposingTeam != null && !isEmpty(opposingTeam.getProbables()))
            {
                int probablePitcherId = opposingTeam.getProbables().get(0).getPlayerId();
                Optional<PlayerAthlete> probablePitcherOptional = cachedProbablePitchers.stream()
                    .filter(pp -> pp.getId().equals(String.valueOf(probablePitcherId))).findFirst();

                PlayerAthlete probablePitcherAthlete = null;
                // if we havent already looked this one up, do so
                if (!probablePitcherOptional.isPresent())
                {
                    probablePitcherAthlete = getPlayerDetails(probablePitcherId);

                    cachedProbablePitchers.add(probablePitcherAthlete);
                } else
                {
                    probablePitcherAthlete = probablePitcherOptional.get();
                }

                probablePitcherAthlete.setDisplayBatsThrows(
                    getThrowArm(probablePitcherAthlete.getDisplayBatsThrows()));

                myPlayer.setOpposingPitcher(probablePitcherAthlete);

                PlayerSplits playerSplits = getPlayerSplits(myPlayer.getRealPlayerId());
                String opsVsLefty = findSplitStatByName(playerSplits, "L", "OPS");
                String opsVsRighty = findSplitStatByName(playerSplits, "R", "OPS");
                myPlayer.setOpsVsLefties(opsVsLefty);
                myPlayer.setOpsVsRighties(opsVsRighty);
            }

            myPlayer.setOpposingTeamHomeAway(opposingTeam.getHomeAway());
            myPlayer.setOpposingTeamId(opposingTeam.getId());

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

    private PlayerSplits getPlayerSplits(int playerId)
    {
        String url = String.format(
            "https://site.web.api.espn.com/apis/common/v3/sports/baseball/mlb/athletes/%s/splits?region=us&lang=en",
            playerId);
        ResponseEntity<PlayerSplits> playerSplitsResponse = restTemplate.getForEntity(
            url,
            PlayerSplits.class);
        return playerSplitsResponse.getBody();
    }

    /**
     * Supported statName: "AB","R","H","2B","3B","HR","RBI","BB","HBP","SO","SB","CS","AVG","OBP","SLG","OPS
     *
     * @param playerSplits
     * @param statName
     * @return
     */
    private String findSplitStatByName(PlayerSplits playerSplits, String opposingPitcherThrows,
        String statName)
    {
        try
        {
            // ESPN reports splits via:
            //        "vs. Left"
            //        "vs. Right"

            String splitTypeToMatch = "vs. Right";
            if (opposingPitcherThrows.equals("L"))
            {
                splitTypeToMatch = "vs. Left";
            }

            String finalSplitTypeToMatch = splitTypeToMatch;

            List<String> statsToMatch = new ArrayList<>();
            if (playerSplits != null)
            {

                playerSplits.getSplitCategories().forEach((splitCategoriesItem ->
                {
                    if (splitCategoriesItem == null || splitCategoriesItem.getSplits() == null)
                    {
                        return;
                    }

                    splitCategoriesItem.getSplits().forEach(split ->
                    {
                        if (split.getDisplayName().equals(finalSplitTypeToMatch))
                        {
                            statsToMatch.addAll(split.getStats());
                        }
                    });
                }));

                int statNameIndex = playerSplits.getLabels().indexOf(statName);
                if (statsToMatch.get(statNameIndex) != null)
                {
                    return statsToMatch.get(statNameIndex);
                } else
                {
                    return "";
                }
            } else
            {
                return "";
            }
        } catch (Exception e)
        {
            logger.error("STAT CALCULATION ERROR OCCURRED!  {} {} {} ", playerSplits,
                opposingPitcherThrows, statName, e);
            return "";
        }
    }

    private String lookupEmailByTeam(int teamId)
    {
        if (teamId == 1)
        {
            return "Matthew.bartolini@gmail.com";
        }
        if (teamId == 2)
        {
            return "jonrhines@gmail.com";
        }
        if (teamId == 3)
        {
            return "kevinmfox@gmail.com";
        }
        if (teamId == 4)
        {
            return "flight.matt@gmail.com";
        }
        if (teamId == 5)
        {
            return "anthony.hurd@gmail.com";
        }
        if (teamId == 6)
        {
            return null;
        }
        if (teamId == 7)
        {
            return "MatthewMBelair@gmail.com";
        }
        if (teamId == 8)
        {
            return null;
        }
        if (teamId == 9)
        {
            return null;
        }
        if (teamId == 10)
        {
            return null;
        }
        if (teamId == 11)
        {
            return null;
        }
        if (teamId == 12)
        {
            return "Benjamin.j.rosenfeld@gmail.com";
        }

        return null;
    }
}
