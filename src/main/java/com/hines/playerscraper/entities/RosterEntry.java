package com.hines.playerscraper.entities;

/**
 * Created by jon on 9/30/19.
 */
public class RosterEntry
{
    int playerId;
    String acquisitionType;
    PlayerPoolEntry playerPoolEntry;

    public int getPlayerId()
    {
        return playerId;
    }

    public void setPlayerId(int playerId)
    {
        this.playerId = playerId;
    }

    public String getAcquisitionType()
    {
        return acquisitionType;
    }

    public void setAcquisitionType(String acquisitionType)
    {
        this.acquisitionType = acquisitionType;
    }

    public PlayerPoolEntry getPlayerPoolEntry()
    {
        return playerPoolEntry;
    }

    public void setPlayerPoolEntry(PlayerPoolEntry playerPoolEntry)
    {
        this.playerPoolEntry = playerPoolEntry;
    }
}
