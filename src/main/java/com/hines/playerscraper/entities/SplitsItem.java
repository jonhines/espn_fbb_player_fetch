package com.hines.playerscraper.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SplitsItem{
	private List<String> stats;
	private String displayName;
	private String abbreviation;
}
