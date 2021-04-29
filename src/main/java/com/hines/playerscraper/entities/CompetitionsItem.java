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
public class CompetitionsItem{
	private String date;
	private String uid;
	private List<CompetitorsItem> competitors;
	private List<Object> notes;
	private String id;
	private String startDate;
	private Status status;
}
