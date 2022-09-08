package com.hines.playerscraper.controllers;


import com.hines.playerscraper.services.TransactionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Set;

@Api(value = "League Transactions")
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


    @ApiOperation(value = "Get all freeAgent additions for a given season")
    @RequestMapping(
        value = "/freeAgentAdds",
        method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllFreeAgentAdds(@RequestParam(value = "year", required = true) String leagueYear)
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", transactionsService.getAllPlayersAddedAsFreeAgent(leagueYear));
        return responseMap;
    }

    @ApiOperation(value = "Get all waiverClaims for a given season")
    @RequestMapping(
        value = "/waiverClaims",
        method = RequestMethod.GET)
    public HashMap<String, Set<String>> getAllWaiverClaims(@RequestParam(value = "year", required = true) String leagueYear)
    {
        HashMap<String, Set<String>> responseMap = new HashMap<>();
        responseMap.put("players", transactionsService.getAllPlayersClaimedByWaivers(leagueYear));
        return responseMap;
    }

    @ApiOperation(value = "Get all freeAgentAdds and waiverClaims in one request, for a given season")
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
