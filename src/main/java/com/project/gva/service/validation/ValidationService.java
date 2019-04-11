package com.project.gva.service.validation;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.project.gva.entity.DeviceTypeEntity;
import com.project.gva.entity.ValidationEntity;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface ValidationService {

    void validateAwsFiles(List<S3ObjectSummary> summaries);
    ValidationEntity buildFileValidation(File file, String name, DeviceTypeEntity deviceType, Date date);
}
