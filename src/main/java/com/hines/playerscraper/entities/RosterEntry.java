package com.hines.playerscraper.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RosterEntry
{
    int playerId;
    String acquisitionType;
    PlayerPoolEntry playerPoolEntry;
}
