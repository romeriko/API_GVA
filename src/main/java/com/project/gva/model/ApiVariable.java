package com.project.gva.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiVariable extends ApiError {

    private String cmd;
    private String name;
    private boolean result;
}
