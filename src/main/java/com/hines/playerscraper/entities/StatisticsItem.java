package com.hines.playerscraper.entities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsItem{
	private String shortDisplayName;
	private String displayValue;
	private String displayName;
	private String name;
	private String rankDisplayValue;
	private String description;
	private int rank;
	private String abbreviation;
	private double value;
	private String type;
}
