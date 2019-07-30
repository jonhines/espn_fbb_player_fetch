package com.hines.playerscraper.entities;

import java.util.List;

public class TopicMessage
{

    public TopicMessage()
    {
    }

    String author;
    List<Message> messages;

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public List<Message> getMessages()
    {
        return messages;
    }

    public void setMessages(List<Message> messages)
    {
        this.messages = messages;
    }
}