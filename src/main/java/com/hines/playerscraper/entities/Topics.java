package com.hines.playerscraper.entities;

import java.util.List;

public class Topics
{

    public Topics()
    {
    }

    List<TopicMessage> topics;

    public List<TopicMessage> getTopics()
    {
        return topics;
    }

    public void setTopics(List<TopicMessage> topics)
    {
        this.topics = topics;
    }
}
