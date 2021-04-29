package com.hines.playerscraper.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Athlete{
	private String displayName;
	private String headshot;
	private String jersey;
	private String fullName;
	private String id;
	private String position;
	private Team team;
	private String shortName;
}
