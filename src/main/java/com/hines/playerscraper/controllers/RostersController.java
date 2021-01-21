package com.hines.playerscraper.controllers;


import com.hines.playerscraper.models.TeamRoster;
import com.hines.playerscraper.services.PlayerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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


}
