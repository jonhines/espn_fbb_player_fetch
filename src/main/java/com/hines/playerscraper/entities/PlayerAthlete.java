package com.hines.playerscraper.entities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerAthlete{
	private String lastName;
	private String displayBatsThrows;
	private String displayName;
	private String fullName;
	private Team team;
	private String type;
	private StatsSummary statsSummary;
	private String uid;
	private String firstName;
	private int debutYear;
	private String jersey;
	private Headshot headshot;
	private String guid;
	private String id;
	private Position position;

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		PlayerAthlete that = (PlayerAthlete) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
