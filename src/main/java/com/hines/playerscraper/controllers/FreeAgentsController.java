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
@RequestMapping(value = "/allFreeAgentAdds")
public class FreeAgentsController
{

    PlayerService playerService;

    @Autowired
    FreeAgentsController(PlayerService playerService)
    {
        this.playerService = playerService;
    }

    @RequestMapping(
        value = "",
        method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllFreeAgentAdds()
    {

        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", playerService.getAllFreeAgentAddsForTheSeason());
        return responseMap;
    }


}
