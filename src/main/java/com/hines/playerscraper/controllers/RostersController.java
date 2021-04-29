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

import java.util.List;

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
    public List<TeamRoster> getTeamRosters(@RequestParam(value = "year", required = true) String leagueYear)
    {
        return playerService.getTeamRosters(leagueYear);
    }


    @ApiOperation(value = "Returns all rosters for each team in The League, for a given year")
    @RequestMapping(
        value = "email",
        method = RequestMethod.GET)
    public ResponseEntity<Object> kickOffEmail(@RequestParam(value = "year", required = true) String leagueYear,
        @RequestParam(value = "apiKey", required = true) String apiKey,
        @RequestParam(value = "teamId", required = true) int teamId) throws AuthenticationException
    {
        // this is the worst auth of all time
        if(!"sirhiss".equals(apiKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("NOPE");
        }
        return ResponseEntity.status(200).body(playerService.sendMatchupSummaryForTeamForToday(leagueYear, teamId));
    }

}
