package com.hines.playerscraper.controllers;


import com.hines.playerscraper.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/")
public class TransactionsController
{

    PlayerService playerService;

    @Autowired
    TransactionsController(PlayerService playerService)
    {
        this.playerService = playerService;
    }

    @RequestMapping(
            value = "/freeAgentAdds",
            method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllFreeAgentAdds()
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", playerService.getAllPlayersAddedAsFreeAgent());
        return responseMap;
    }

    @RequestMapping(
            value = "/waiverClaims",
            method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllWaiverClaims()
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", playerService.getAllPlayersClaimedByWaivers());
        return responseMap;
    }

}
