package com.project.gva.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApiDevice extends ApiError {
    private String name;
    private String id;
    private Types.DeviceStatus status;
}
