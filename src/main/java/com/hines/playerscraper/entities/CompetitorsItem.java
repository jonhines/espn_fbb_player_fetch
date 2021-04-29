package com.hines.playerscraper.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompetitorsItem {
	private String uid;
	private String homeAway;
	private String id;
	private Team team;
	private String type;
	private List<ProbablesItem> probables;
	private int order;
}
