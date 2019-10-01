package com.hines.playerscraper.controllers;


import com.hines.playerscraper.models.TeamRoster;
import com.hines.playerscraper.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public List<TeamRoster> getTeamRosters()
    {
        return playerService.getTeamRosters();
    }


}
