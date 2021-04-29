package com.hines.playerscraper;

import com.hines.playerscraper.entities.Message;
import com.hines.playerscraper.entities.Player;
import com.hines.playerscraper.entities.Topics;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class PlayerScrape
{


    @Test
    public void getAllLeagueFreeAgentAdds()
    {


        LocalDate d = LocalDate.now(ZoneId.of("America/New_York"));
        String dateToFetchSummaryFor = d.format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        RestTemplate restTemplate = new RestTemplate();

        String filters = "{\"topics\":{\"filterType\":{\"value\":[\"ACTIVITY_TRANSACTIONS\"]},\"limit\":2000,\"limitPerMessageSet\":{\"value\":2000},\"offset\":0,\"sortMessageDate\":{\"sortPriority\":1,\"sortAsc\":false},\"sortFor\":{\"sortPriority\":2,\"sortAsc\":false},\"filterDateRange\":{\"value\":1553832000000,\"additionalValue\":1564459199999},\"filterIncludeMessageTypeIds\":{\"value\":[178,180]}}}";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer",
            "http://fantasy.espn.com/baseball/recentactivity?leagueId=30710&endDate=20190729&seasonId=2019&startDate=20190727&teamId=-1&transactionType=2&activityType=2&page=1");
        headers.add("X-Fantasy-Source", "kona");
        headers.add("User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        headers.add("X-Fantasy-Filter", filters);
        headers.add("X-Fantasy-Platform", "kona-PROD-6b1bde3ecf8dde941512a5c0d02d8fd8a7461f47");
        headers.add("Origin", "http://fantasy.espn.com");
        headers.add("Cache-Control", "no-cache");
        headers.add("Host", "fantasy.espn.com");
        headers.add("Cookie",
            "espn_s2=AEAwiDIR7Un6znzyyrhqnfVQX4wb2tGm292n3kkeDgvuZNNM98L2KTQuXuO5bVyd0qCjrjxX0gk%2B7SWcDuNljIKb0KKntT43UYeNgeerd6ar16B0jlor1wbrX6wBReMQhI1vriQ8uUuh2RP58%2BBOfpdEcTImfla8T8H5L9uDD%2FwPZh%2FJKw8iSAVYuHu579OVXupmxeWBJ3mb%2B9ZxtTVzb%2FZC%2FMNODCTLGOp9U8w1YKoE86p8GgCfhP8qt%2BrTzQqWgD%2B0y2IesAObnSXTTa53YkYhlMta9SV%2BnOoDfjq96XDbJg%3D%3D; SWID={FF4191B2-668A-44DE-951C-6D8C351B60C1}");
        headers.add("Connection", "keep-alive");
        headers.add("cache-control", "no-cache");

        try
        {
            HttpEntity<Topics> entity = new HttpEntity<>(headers);

            ResponseEntity<Topics> responseEntity = restTemplate.exchange(
                "https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/segments/0/leagues/30710/communication/?view=kona_league_communication",
                HttpMethod.GET,
                entity,
                Topics.class);

            Topics body = responseEntity.getBody();
            Set<String> playersAddedAsFreeAgent = new HashSet<>();
            // https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/players/%s?scoringPeriodId=0&view=players_wl
            body.getTopics().stream().forEach(topic ->
            {

                // when polling only for FA Adds, there will only be a single message, make this more dynamic later
                Message message = topic.getMessages().get(0);
                String url = String.format("https://fantasy.espn.com/apis/v3/games/flb/seasons/2019/players/%s?scoringPeriodId=0&view=players_wl",
                    message.getTargetId());
                try
                {

                    ResponseEntity<Player> playerResponseEntity = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        Player.class);

                    playersAddedAsFreeAgent.add(playerResponseEntity.getBody().getFullName());

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });

            System.out.println(playersAddedAsFreeAgent);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
