package com.hines.playerscraper.entities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Type{
	private String name;
	private String description;
	private String id;
	private String state;
	private boolean completed;
	private String detail;
	private String shortDetail;
}
