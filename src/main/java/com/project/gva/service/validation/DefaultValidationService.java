package com.project.gva.service.validation;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.Tag;
import com.project.gva.entity.DeviceTypeEntity;
import com.project.gva.entity.UpdateEntity;
import com.project.gva.entity.ValidationEntity;
import com.project.gva.exception.NotFoundException;
import com.project.gva.model.FileValidation;
import com.project.gva.model.Types;
import com.project.gva.repository.DeviceTypeRepository;
import com.project.gva.repository.UpdateRepository;
import com.project.gva.repository.ValidationRepository;
import com.project.gva.service.aws.AwsService;
import com.project.gva.service.message.MessageService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Log
@Service(value = "defaultValidationService")
public class DefaultValidationService implements ValidationService {


    private final
    ValidationRepository validationRepository;

    private final
    AwsService awsService;

    private final
    DeviceTypeRepository deviceTypeRepository;

    private final
    UpdateRepository updateRepository;

    @Value(value = "${app.sms.receiver}")
    private String SMS_RECEIVER;

    private final
    MessageService messageService;


    @Autowired
    public DefaultValidationService(ValidationRepository validationRepository, AwsService awsService, DeviceTypeRepository deviceTypeRepository, UpdateRepository updateRepository, MessageService messageService) {
        this.validationRepository = validationRepository;
        this.awsService = awsService;
        this.deviceTypeRepository = deviceTypeRepository;
        this.updateRepository = updateRepository;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public void validateAwsFiles(List<S3ObjectSummary> summaries) {
        summaries.remove(0);
        summaries.forEach(summary -> {
            ValidationEntity validation = validationRepository.findByNameEquals(getKeyName(summary)).orElse(null);

            if (Objects.isNull(validation)) {
                buildValidation(summary);
            }
        });
    }

    private void buildValidation(S3ObjectSummary summary) {

        String name = getKeyName(summary);
        String finalName = getRealName(name);

        Types.Device dt = Types.Device.of(name);
        DeviceTypeEntity deviceType = deviceTypeRepository.findByDescriptionEquals(dt.name()).orElseThrow(NotFoundException::new);

        File file = awsService.download(finalName, Types.File.UPDATE);
        ValidationEntity validation = buildFileValidation(file, name, deviceType, summary.getLastModified());

        validation = this.validationRepository.save(validation);

        buildUpdateRegistry(summary, validation, finalName); //LLAMA A GUARDAR LA INFORMACION DE LA ACTUALIZACION EN BASE DE DATOS.

    }

    @Override
    public ValidationEntity buildFileValidation(File file, String name, DeviceTypeEntity deviceType, Date date) {
        FileValidation fileValidation = FileValidation.of(file);
        return ValidationEntity.builder()
                .bytes(fileValidation.getByteCount())
                .ckSum(fileValidation.getCksum())
                .crc32(fileValidation.getCrc32())
                .sha256(fileValidation.getSha256())
                .name(name)
                .type(deviceType)
                .source("AWS")
                .uuid(UUID.randomUUID().toString())
                .date(date)
                .build();
    }

    private void buildUpdateRegistry(S3ObjectSummary summary, ValidationEntity validation, String name) {
        List<Tag> taggingResult = this.awsService.downloadTagging(name, Types.File.UPDATE);
        UpdateEntity.UpdateEntityBuilder update = UpdateEntity.builder().creationDate(summary.getLastModified()).name(name).validation(validation);

        taggingResult.forEach(tag -> {
            log.info(tag.getKey().toUpperCase() + ", " + summary.getKey());
            switch (Types.Tag.of(tag.getKey())) {
                case TYPE: {
                    Types.Device dt = Types.Device.of(tag.getValue());
                    DeviceTypeEntity deviceType = deviceTypeRepository.findByDescriptionEquals(dt.name()).orElseThrow(NotFoundException::new);
                    update.type(deviceType);
                    break;
                }
                case VERSION: {
                    update.version(tag.getValue());
                    break;
                }
                case COMENTARIO:
                    update.comments(tag.getValue());
            }
        });
        UpdateEntity updateEntity = this.updateRepository.save(update.build());
        String message = String.format("Hay una nueva actualizacion para sus dispositivos: %s, Nueva Version: %s", updateEntity.getType().getDescription().toUpperCase(), updateEntity.getVersion());
        messageService.sendSms(SMS_RECEIVER, message, null, Types.MessageTopic.PROCESO_ACTUALIZACION, Types.MessageStatus.INFO, updateEntity.getName().concat(" - V").concat(updateEntity.getVersion()));

    }


    private String getKeyName(S3ObjectSummary summary) {
        return (summary.getKey().split("/")[1]);
    }

    private String getRealName(String name) {
        return name.split("\\.")[0];
    }
}
