package com.project.gva.model;

import lombok.Data;

import javax.persistence.Transient;

@Data
public class ApiError {

    @Transient
    private boolean ok;

    @Transient
    private String error;
}
