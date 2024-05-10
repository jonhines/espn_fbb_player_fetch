package com.hines.playerscraper.services;

import com.hines.playerscraper.entities.Message;
import com.hines.playerscraper.entities.Player;
import com.hines.playerscraper.entities.FreeAgentContainer;
import com.hines.playerscraper.entities.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Service
public class TransactionsService extends ESPNService
{

    private static final Logger logger = LoggerFactory.getLogger(TransactionsService.class);


    private RestTemplate restTemplate;
    private PlayerService playerService;

    public TransactionsService(PlayerService playerService, RestTemplate restTemplate)
    {
        this.playerService = playerService;
        this.restTemplate = restTemplate;
    }

    public HashSet<String> getAllPlayersClaimedByWaivers(String leagueYear)
    {
        HashSet<String> playerNames = new HashSet<>();

        long startDateInMs = getRequestDate(leagueYear + "/03/28 00:00:00");
        long endDateInMs = getRequestDate(leagueYear + "/10/01 00:00:00");

        int messageTypeWaiverClaim = 180;

        String filters =
            "{\"topics\":{\"filterType\":{\"value\":[\"ACTIVITY_TRANSACTIONS\"]},\"limit\":2000,\"limitPerMessageSet\":{\"value\":2000},\"offset\":0,\"sortMessageDate\":{\"sortPriority\":1,\"sortAsc\":false},\"sortFor\":{\"sortPriority\":2,\"sortAsc\":false},\"filterDateRange\":{\"value\":"
                + startDateInMs + ",\"additionalValue\":" + endDateInMs
                + "},\"filterIncludeMessageTypeIds\":{\"value\":[" + messageTypeWaiverClaim
                + "]}}}";

        playerNames = getTransactions(leagueYear, filters);

        return playerNames;
    }

    public HashSet<String> getAllPlayersAddedAsFreeAgent(String leagueYear)
    {
        HashSet<String> playerNames = new HashSet<>();

        long startDateInMs = getRequestDate(leagueYear + "/03/28 00:00:00");
        long endDateInMs = getRequestDate(leagueYear + "/10/01 00:00:00");

        int messageTypeFreeAgentAdd = 178;

        String filters =
            "{\"topics\":{\"filterType\":{\"value\":[\"ACTIVITY_TRANSACTIONS\"]},\"limit\":2000,\"limitPerMessageSet\":{\"value\":2000},\"offset\":0,\"sortMessageDate\":{\"sortPriority\":1,\"sortAsc\":false},\"sortFor\":{\"sortPriority\":2,\"sortAsc\":false},\"filterDateRange\":{\"value\":"
                + startDateInMs + ",\"additionalValue\":" + endDateInMs
                + "},\"filterIncludeMessageTypeIds\":{\"value\":[" + messageTypeFreeAgentAdd
                + "]}}}";

        playerNames = getTransactions(leagueYear, filters);

        return playerNames;
    }


    /**
     * Return the names of all players that were added as a FreeAgent or Waiver
     *
     * @return
     */
    private HashSet<String> getTransactions(String leagueYear, String filters)
    {
        HashSet<String> playerNames = new HashSet<>();

        HttpHeaders headers = getEspnRequestHeaders(leagueYear, filters);

        try
        {
            HttpEntity<Topics> entity = new HttpEntity<>(headers);
            String url = "https://lm-api-reads.fantasy.espn.com/apis/v3/games/flb/seasons/" + leagueYear
                    + "/segments/0/leagues/" + LEAGUE_ID
                    + "/communication/?view=kona_league_communication";
            ResponseEntity<Topics> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Topics.class);

            Topics body = responseEntity.getBody();
            // https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/players/%s?scoringPeriodId=0&view=players_wl
            body.getTopics().parallelStream().forEach(topic ->
            {
                // when polling only for FA Adds, there will only be a single message, make this more dynamic later
                Message message = topic.getMessages().get(0);
                CompletableFuture<Player> playerFuture = playerService
                    .getPlayerByIdAsync(leagueYear, message.getTargetId());
                try
                {
                    playerNames.add(playerFuture.get(2, TimeUnit.SECONDS).getFullName());
                } catch (ExecutionException | InterruptedException | TimeoutException e)
                {
                    logger.error("concurrent issue fetching player, ignoring!", e);
                }
            });

            return playerNames;

        } catch (Exception e)
        {
            logger.error("An error occurred fetching all transactions for the league.", e);
            throw new RuntimeException(
                "An error occurred fetching information from ESPN API! " + e.getMessage());
        }
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
