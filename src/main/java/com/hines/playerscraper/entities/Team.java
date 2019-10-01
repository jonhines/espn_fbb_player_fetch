package com.hines.playerscraper.entities;

/**
 * Created by jon on 9/30/19.
 */
public class Team
{
    String abbrev;
    int id;
    String location;
    String nickname;
    Roster roster;

    public String getAbbrev()
    {
        return abbrev;
    }

    public void setAbbrev(String abbrev)
    {
        this.abbrev = abbrev;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public Roster getRoster()
    {
        return roster;
    }

    public void setRoster(Roster roster)
    {
        this.roster = roster;
    }
}
