package com.hines.playerscraper.entities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Position{
	private String displayName;
	private String name;
	private String id;
	private String abbreviation;
	private boolean leaf;
	private String slug;
}
