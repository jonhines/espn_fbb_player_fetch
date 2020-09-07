package com.hines.playerscraper.services;

import org.springframework.http.HttpHeaders;

public abstract class ESPNService
{
    public final static String LEAGUE_ID = "30710";

    protected HttpHeaders getEspnRequestHeaders(String leagueYear, String filters)
    {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer",
            "http://fantasy.espn.com/baseball/recentactivity?leagueId=" + LEAGUE_ID + "&endDate=20190729&seasonId=" + leagueYear
                + "&startDate=20190727&teamId=-1&transactionType=2&activityType=2&page=1");
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

}
