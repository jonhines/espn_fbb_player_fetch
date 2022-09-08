package com.hines.playerscraper.controllers;


import com.hines.playerscraper.models.TeamRoster;
import com.hines.playerscraper.services.PlayerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.auth.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Api(value = "League Rosters")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/rosters")
public class RostersController
{

    PlayerService playerService;

    @Autowired
    RostersController(PlayerService playerService)
    {
        this.playerService = playerService;
    }

    @ApiOperation(value = "Returns all rosters for each team in The League, for a given year")
    @RequestMapping(
        value = "",
        method = RequestMethod.GET)
    public List<TeamRoster> getTeamRosters(
        @RequestParam(value = "year", required = true) String leagueYear)
    {
        return playerService.getTeamRosters(leagueYear);
    }


    @ApiOperation(value = "Returns all rosters for each team in The League, for a given year")
    @RequestMapping(
        value = "email",
        method = RequestMethod.GET)
    public ResponseEntity<Object> kickOffEmail(
        @RequestParam(value = "year", required = true) String leagueYear,
        @RequestParam(value = "apiKey", required = true) String apiKey,
        @RequestParam(value = "teamId", required = true) Set<Integer> teamIds)
        throws AuthenticationException
    {
        // this is the worst auth of all time
        if (!"sirhiss".equals(apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("NOPE");
        }
        playerService.sendMatchupSummaryForTeamsForToday(leagueYear, teamIds);
        return ResponseEntity.status(200).body("SENT!");
    }


    @ApiOperation(value = "Send an email for potential freeAgent bats to pickup")
    @RequestMapping(
        value = "email/freeAgentBats",
        method = RequestMethod.GET)
    public ResponseEntity<Object> freeAgentBatsEmailSend(
        @RequestParam(value = "year", required = true) String leagueYear,
        @RequestParam(value = "apiKey", required = true) String apiKey,
        @RequestParam(value = "numPlayers", required = true, defaultValue = "50") int numPlayers)
    {
        // this is the worst auth of all time
        if (!"sirhiss".equals(apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }
        playerService.sendFreeAgentBatsToConsiderEmail(leagueYear, numPlayers);
        return ResponseEntity.status(200).body("SENT!");
    }
}
