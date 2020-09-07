package com.hines.playerscraper.controllers;


import com.hines.playerscraper.services.TransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/")
public class TransactionsController
{

    TransactionsService transactionsService;

    @Autowired
    TransactionsController(TransactionsService transactionsService)
    {
        this.transactionsService = transactionsService;
    }

    @RequestMapping(
        value = "/freeAgentAdds",
        method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllFreeAgentAdds(@RequestParam(value = "year", required = true) String leagueYear)
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", transactionsService.getAllPlayersAddedAsFreeAgent(leagueYear));
        return responseMap;
    }

    @RequestMapping(
        value = "/waiverClaims",
        method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllWaiverClaims(@RequestParam(value = "year", required = true) String leagueYear)
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", transactionsService.getAllPlayersClaimedByWaivers(leagueYear));
        return responseMap;
    }

    @RequestMapping(
        value = "/allTransactions",
        method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllTransactions(@RequestParam(value = "year", required = true) String leagueYear)
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("waiverAdds", transactionsService.getAllPlayersClaimedByWaivers(leagueYear));
        responseMap.put("freeAgentAdds", transactionsService.getAllPlayersAddedAsFreeAgent(leagueYear));
        return responseMap;
    }

}
