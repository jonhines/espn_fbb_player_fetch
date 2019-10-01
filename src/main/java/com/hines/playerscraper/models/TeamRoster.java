package com.hines.playerscraper.models;

import java.util.Set;

/**
 *
 */
public class TeamRoster
{
    String name;
    Set<String> roster;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Set<String> getRoster()
    {
        return roster;
    }

    public void setRoster(Set<String> roster)
    {
        this.roster = roster;
    }
}
