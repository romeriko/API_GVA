package com.project.gva.scheduling;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.project.gva.entity.*;
import com.project.gva.model.ApiDevice;
import com.project.gva.model.Types;
import com.project.gva.repository.DeviceRepository;
import com.project.gva.repository.InstallationRepository;
import com.project.gva.repository.ReadDeviceRepository;
import com.project.gva.repository.UpdateRepository;
import com.project.gva.service.aws.AwsService;
import com.project.gva.service.device.ParticleService;
import com.project.gva.service.message.MessageService;
import com.project.gva.service.validation.ValidationService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Log
@Component
@SuppressWarnings("Duplicates")
public class ScheduledJobs {

    private final
    AwsService awsService;

    private final
    MessageService messageService;

    private final
    ParticleService particleService;

    private final
    ValidationService validationService;

    private final
    ReadDeviceRepository readDeviceRepository;

    private final
    InstallationRepository installationRepository;

    private final
    DeviceRepository deviceRepository;

    private final
    UpdateRepository updateRepository;

    @Value(value = "${app.sms.receiver}")
    private String SMS_RECEIVER;

    @Autowired
    public ScheduledJobs(AwsService awsService,
                         ValidationService validationService,
                         ParticleService particleService,
                         ReadDeviceRepository readDeviceRepository,
                         InstallationRepository installationRepository,
                         DeviceRepository deviceRepository,
                         UpdateRepository updateRepository, MessageService messageService) {

        this.awsService = awsService;
        this.validationService = validationService;
        this.particleService = particleService;
        this.readDeviceRepository = readDeviceRepository;
        this.installationRepository = installationRepository;

        this.deviceRepository = deviceRepository;
        this.updateRepository = updateRepository;
        this.messageService = messageService;
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void readFilesAndUpdateValidation() {
        ListObjectsV2Result files = awsService.readFiles(Types.File.UPDATE);
        validationService.validateAwsFiles(files.getObjectSummaries());
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void readAllDeviceStatus() {
        List<ReadDevice> devices = particleService.getAllDevicesStatus();
        devices.forEach(device -> {
            device.setId(UUID.randomUUID().toString());
//            log.info("Updating status for device: " + device.toString());
            device = this.readDeviceRepository.save(device);
            updateDeviceStatus(device.getLogDevice(), device.getStatus());
        });
    }

    private void updateDeviceStatus(String id, Types.DeviceStatus status) {
        DeviceEntity device = this.deviceRepository.findById(id).orElse(null);
        if (Objects.isNull(device))
            return;
        if (status.equals(Types.DeviceStatus.ONLINE)) {
            device.setLastConnection(new Date());
        }
        device.setStatus(status);
        this.deviceRepository.save(device);
    }

    @Transactional
    @Scheduled(cron = "*/5 * * * * *")
    public void execValidateVersions() {
        List<ApiDevice> devices = particleService.getAllDevices();
        devices.forEach(device -> {
            DeviceEntity deviceEntity = this.deviceRepository.findById(device.getId()).orElse(null);
            if (Objects.nonNull(deviceEntity) && deviceEntity.getAutomaticUpdated()) {
                InstallationEntity installation = this.installationRepository.findByCurrentlyInstalledEqualsAndDeviceEquals(true, deviceEntity);
                if (Objects.isNull(installation))
                    return;
                deviceEntity.setVersion(installation.getUpdate().getName().concat(" - v").concat(installation.getUpdate().getVersion()));
                this.deviceRepository.save(deviceEntity);
                List<UpdateEntity> updates = this.updateRepository.findAllByTypeEqualsAndVersionGreaterThan(deviceEntity.getTypeEntity(), installation.getUpdate().getVersion());


                if (Objects.nonNull(updates) && updates.size() > 0) {
                    UpdateEntity update = updates.get(updates.size() - 1);
                    File file = awsService.download(update.getName(), Types.File.UPDATE);

                    ValidationEntity validation = validationService.buildFileValidation(file, file.getName(), deviceEntity.getTypeEntity(), new Date());

                    boolean valid = validation.isValid(update.getValidation());

                    if (!valid) {
                        if (deviceEntity.getAllowCorrupt()) {
                            InstallationEntity wasInstalled = this.particleService.execInstall(installation, deviceEntity, file, update, false);
                            if (Objects.nonNull(wasInstalled)) {
                                String message = String.format("El dispositivo: %s, se actualizó a una versión no válida: %s", deviceEntity.getName(), update.getVersion());
                                messageService.sendSms(SMS_RECEIVER, message, deviceEntity.getId(), Types.MessageTopic.PROCESO_ACTUALIZACION, Types.MessageStatus.WARNING, wasInstalled.getId());
                                return;
                            }
                        } else {
                            String message = String.format("Fue detectada una actualización no válida: %s - V%s, para el dispositivo: %s. NO FUE INSTALADA", update.getName(), update.getVersion(), deviceEntity.getName());
                            messageService.sendSms(SMS_RECEIVER, message, deviceEntity.getId(), Types.MessageTopic.PROCESO_ACTUALIZACION, Types.MessageStatus.WARNING, update.getId().toString());
                            return;
                        }
                    }
                    InstallationEntity wasInstalled = this.particleService.execInstall(installation, deviceEntity, file, update, valid);
                    if (Objects.nonNull(wasInstalled)) {
                        String message = String.format("El dispositivo: %s, se actualizó a la versión: %s", deviceEntity.getName(), update.getVersion());
                        messageService.sendSms(SMS_RECEIVER, message, deviceEntity.getId(), Types.MessageTopic.PROCESO_ACTUALIZACION, Types.MessageStatus.SUCCESS, update.getId().toString());
                    }
                }
            }

        });
    }
}