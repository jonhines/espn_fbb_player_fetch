package com.hines.playerscraper.entities;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player
{

    int id;
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

    public int getRealPlayerId()
    {
        if(id >= 0)
        {
            return id;
        }
        return playerId;
    }

    @JsonProperty("opsVsOpposingPitcher")
    public double getOPSBasedOnOpposingPitcher()
    {
        if(opposingPitcher == null)
        {
            return 0;
        }
        try
        {
            String opposingPitcherThrows = getThrowArm(opposingPitcher.getDisplayBatsThrows());
            if(StringUtils.equals(opposingPitcherThrows, "L"))
            {
                return Double.valueOf(opsVsLefties);
            }
            return Double.valueOf(opsVsRighties);
        } catch(Exception e){
            return 0;
        }
    }

    /**
     * Assumes displayBatsThrows is a slash delimited value where the first value is hit, second is
     * throw: Right/Right or Left/Right
     *
     * @param displayBatsThrows
     * @return R or L
     */
    public static String getThrowArm(String displayBatsThrows)
    {
        // sometimes players dont have the delimited value
        if (displayBatsThrows == null || displayBatsThrows.length() == 1)
        {
            return displayBatsThrows;
        }
        String[] split = displayBatsThrows.split("/");
        String val = split[1];
        return String.valueOf(val.charAt(0));
    }

    /**
     * Assumes displayBatsThrows is a slash delimited value where the first value is hit, second is
     * throw: Right/Right or Left/Right
     *
     * @param displayBatsThrows
     * @return R or L
     */
    public static String getHitHandedness(String displayBatsThrows)
    {
        // sometimes players dont have the delimited value
        if (displayBatsThrows == null || displayBatsThrows.length() == 1)
        {
            return displayBatsThrows;
        }
        String[] split = displayBatsThrows.split("/");
        String val = split[0];
        return String.valueOf(val.charAt(0));
    }

    /**
     * Get position name of baseball position num
     *
     * @param defaultPositionId
     * @return
     */
    public String getPositionName()
    {
        if (defaultPositionId == 2)
        {
            return "C";
        }
        if (defaultPositionId == 3)
        {
            return "1B";
        }
        if (defaultPositionId == 4)
        {
            return "2B";
        }
        if (defaultPositionId == 5)
        {
            return "3B";
        }
        if (defaultPositionId == 6)
        {
            return "SS";
        }
        if (defaultPositionId == 7 || defaultPositionId == 8 || defaultPositionId == 9)
        {
            return "OF";
        }
        return "UTIL";
    }

}
