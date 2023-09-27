package com.klasha.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class ApiResponse<D,E> implements Serializable{
    private String message;
    private int status;
    private D data;
    private E errorData;
    private Instant timestamp;
}
