package com.hines.playerscraper.entities;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail
{

    private String error;
    private String exception;
    private Integer status;
    private String message;
    private String path;
    private Date timestamp;

}
