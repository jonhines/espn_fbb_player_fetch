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
public class StatsSummary{
	private String displayName;
	private List<StatisticsItem> statistics;
}
