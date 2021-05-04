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
public class PlayerSplits
{

    private List<SplitCategoriesItem> splitCategories;
    private String displayName;
    private List<String> labels;
}
