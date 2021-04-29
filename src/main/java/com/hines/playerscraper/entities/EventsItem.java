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
public class EventsItem{
	private String date;
	private String uid;
	private String name;
	private List<CompetitionsItem> competitions;
	private String id;
	private String shortName;
}
