package com.project.gva.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiFunction extends ApiError {
    private String id;
    private String name;
    private boolean connected;
    @JsonProperty(value = "return_value")
    private String value;
}
