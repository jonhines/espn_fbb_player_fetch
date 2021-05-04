package com.hines.playerscraper.entities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player
{

    int playerId;
    String fullName;
    int defaultPositionId;
    int proTeamId;
    boolean injured;
    String displayBatsThrows;

    String positionName;
    String opposingTeamId;
    String opposingTeamHomeAway;
    String opposingTeamName;
    String gameTime;
    String opposingTeamSummary;
    String opsVsLefties;
    String opsVsRighties;
    PlayerAthlete opposingPitcher;

}
