package com.hines.playerscraper.entities;

import java.util.List;

/**
 * Created by jon on 9/30/19.
 */
public class Roster
{
    List<RosterEntry> entries;

    public List<RosterEntry> getEntries()
    {
        return entries;
    }

    public void setEntries(List<RosterEntry> entries)
    {
        this.entries = entries;
    }
}
