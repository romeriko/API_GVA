package com.project.gva.resource;

import com.project.gva.entity.*;
import com.project.gva.exception.ForbiddenException;
import com.project.gva.exception.NotFoundException;
import com.project.gva.model.*;
import com.project.gva.repository.*;
import com.project.gva.service.aws.AwsService;
import com.project.gva.service.device.ParticleService;
import com.project.gva.service.validation.ValidationService;
import lombok.extern.java.Log;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Log
@RestController
@SuppressWarnings("Duplicates")
@RequestMapping(value = "device")
public class DeviceResource {

    private final
    DeviceRepository deviceRepository;

    private final
    DeviceTypeRepository deviceTypeRepository;

    private final
    UpdateRepository updateRepository;

    private final
    ReadDeviceRepository readDeviceRepository;

    private final
    InstallationRepository installationRepository;

    private final
    AlertRepository alertRepository;

    private final
    UserRepository userRepository;

    private final
    ParticleService particleService;

    private final
    AwsService awsService;

    private final
    ValidationService validationService;

    @Autowired
    public DeviceResource(DeviceTypeRepository deviceTypeRepository, DeviceRepository deviceRepository, UpdateRepository updateRepository, ReadDeviceRepository readDeviceRepository, InstallationRepository installationRepository, AlertRepository alertRepository, UserRepository userRepository, ParticleService particleService, AwsService awsService, ValidationService validationService) {
        this.deviceTypeRepository = deviceTypeRepository;
        this.deviceRepository = deviceRepository;
        this.updateRepository = updateRepository;
        this.readDeviceRepository = readDeviceRepository;
        this.installationRepository = installationRepository;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.particleService = particleService;
        this.awsService = awsService;
        this.validationService = validationService;
    }

    @GetMapping(value = {"/", ""})
    public ResponseEntity getAllDevices(String device) {
        if (Objects.isNull(device)) {
            List<DeviceEntity> deviceEntities = this.deviceRepository.findAllByClaimTrue();
            return mapResponse(deviceEntities);
        }
        DeviceEntity entity = this.deviceRepository.findByIdAndClaimTrue(device).orElseThrow(NotFoundException::new);
        return ResponseEntity.ok(entity);
    }

    @PostMapping(value = "add")
    public ResponseEntity addNewDevice(String id) {
        int code = this.particleService.claimDevice(id);
        return ResponseEntity.status(code).build();
    }

    @DeleteMapping(value = "remove")
    public ResponseEntity removeDevice(String deviceId, Long user, String password) {

        UserEntity userEntity = this.userRepository.findById(user).orElseThrow(NotFoundException::new);

        if (BCrypt.checkpw(password, userEntity.getPassword())) {
            int code = this.particleService.unClaimDevice(deviceId);
            if (code != 200)
                return ResponseEntity.status(code).body("No se pudo eliminar el dispositivo del cliente.");
            DeviceEntity device = this.deviceRepository.findById(deviceId).orElseThrow(NotFoundException::new);
            device.setClaim(false);
            device = this.deviceRepository.saveAndFlush(device);
            return ResponseEntity.ok(device);
        }
        return ResponseEntity.status(401).body("INCORRECT PASSWORD");
    }

    @PostMapping
    public ResponseEntity saveDevice(@RequestBody DeviceEntity device) {
        DeviceTypeEntity type = this.deviceTypeRepository.findByDescriptionEquals(device.getTypeName()).orElse(null);
        device.setType(type);
        checkRequiredFields(device);
        if (exists(device.getId()) && device.isOld()) {
            return ResponseEntity.ok(this.deviceRepository.saveAndFlush(device));
        } else if (exists(device.getId())) {
            return ResponseEntity.unprocessableEntity().body("{\"message\": \"El dispositivo que intenta agregar ya esta registrado.\"}");
        }
        int code = this.particleService.claimDevice(device.getId());
        if (code != 200)
            return ResponseEntity.status(code).body("{\"message\": \"No se pudo agregar el dispositivo especificado\"}");
        return ResponseEntity.ok(this.deviceRepository.saveAndFlush(device));
    }

    private void checkRequiredFields(DeviceEntity device) {
        if (device.getLastConnection() == null)
            device.setLastConnection(new Date());
        if (device.getLastUpdate() == null)
            device.setLastUpdate(new Date());
        if (device.getStatus() == null)
            device.setStatus(Types.DeviceStatus.OFFLINE);
        if (device.getClaim() == null)
            device.setClaim(true);
        device.setUsername("ROOT");
        if (device.getVersion() == null)
            device.setVersion("0.0.0");
    }

    private boolean exists(String id) {
        DeviceEntity entity = this.deviceRepository.findById(id).orElse(null);
        return entity != null;
    }

    @GetMapping(value = "types")
    public ResponseEntity getAllDeviceTypes() {
        List<DeviceTypeEntity> deviceEntities = this.deviceTypeRepository.findAll();
        return mapResponse(deviceEntities);
    }

    @GetMapping(value = "updates")
    public ResponseEntity getUpdates(String device) {
        List<UpdateEntity> updates;
        if (Objects.isNull(device))
            updates = this.updateRepository.findAll();
        else {
            DeviceTypeEntity type = this.deviceTypeRepository.findByDescriptionEquals(device).orElse(null);
            if (Objects.nonNull(type))
                updates = this.updateRepository.findAllByTypeEqualsAndVersionGreaterThan(type, "0");
            else updates = this.updateRepository.findAll();
        }
        if (updates.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updates);
    }

    @GetMapping(value = "update/{device}")
    public ResponseEntity getUpdates(@PathVariable(name = "device") String deviceId, String version) {
        DeviceEntity device = this.deviceRepository.findById(deviceId).orElseThrow(NotFoundException::new);
        List<UpdateEntity> updates = this.updateRepository.findAllByTypeEqualsAndVersionGreaterThan(device.getTypeEntity(), version);
        return ResponseEntity.ok(updates);
    }

    @PostMapping(value = "status")
    public ResponseEntity<ReadDevice> updateStatus(@RequestBody ReadDevice device) {
        device.setDate(new Date());
        device.setId(UUID.randomUUID().toString());
//        log.info("Updating status for device: " + device.toString());
        this.readDeviceRepository.save(device);
        return ResponseEntity.status(200).body(device);
    }

    @GetMapping(value = "logs")
    public ResponseEntity getDeviceStatus(PageObject page, @RequestParam String id) {
        DeviceEntity device = deviceRepository.findById(id).orElseThrow(NotFoundException::new);
        Pageable pageable = PageRequest.of(page.getPage(), page.getSize(), new Sort(page.getOrder(), page.getField()));
        Page devices = this.readDeviceRepository.findAllByLogDeviceEquals(device.getId(), pageable);
        return ResponseEntity.ok(devices.getContent());
    }

    @GetMapping(value = "installations")
    public ResponseEntity getCurrentlyInstalled() {
        List<InstallationEntity> installationEntities = installationRepository.findAllByCurrentlyInstalled(true);
        return ResponseEntity.ok(installationEntities);
    }

    @Transactional
    @PostMapping(value = "installations")
    public ResponseEntity installVersion(@RequestBody InstallUpdate update) {
        UserEntity user = this.userRepository.findById(update.getUser()).orElseThrow(NotFoundException::new);

        if (BCrypt.checkpw(update.getPassword(), user.getPassword())) {
            DeviceEntity device = this.deviceRepository.findById(update.getDevice()).orElseThrow(NotFoundException::new);
            InstallationEntity installation = this.installationRepository.findByCurrentlyInstalledEqualsAndDeviceEquals(true, device);

            UpdateEntity updateEntity = this.updateRepository.findById(update.getUpdate()).orElseThrow(NotFoundException::new);
            File file = awsService.download(updateEntity.getName(), Types.File.UPDATE);
            ValidationEntity validation = validationService.buildFileValidation(file, file.getName(), device.getTypeEntity(), new Date());


            //BUILD NEW INSTALLATION ENTITY
            boolean valid = validation.isValid(updateEntity.getValidation());
            InstallationEntity install = this.particleService.execInstall(installation, device, file, updateEntity, valid);

            if (Objects.isNull(install))
                throw new ForbiddenException("COULD NO INSTALL THE SPECIFIED VERSION ON THE SELECTED DEVICE");
            //UPDATE DEVICE FIELDS
            device.setLastUpdate(new Date());
            device.setVersion(install.getUpdate().getName().concat(" - v").concat(install.getUpdate().getVersion()));
            device.setAutomaticUpdated(false);
            this.deviceRepository.saveAndFlush(device);

            return ResponseEntity.ok(install);
        }
        return ResponseEntity.status(401).body("INCORRECT PASSWORD");
    }

    @GetMapping(value = "alert")
    public ResponseEntity getAlerts() {
        return ResponseEntity.ok(this.alertRepository.findAllByOrderByDateDesc());
    }

    @GetMapping(value = "alert/install")
    public ResponseEntity getAlertsInstall() {
        List<AlertEntity> alerts = this.alertRepository.findAllByTopicEquals(Types.MessageTopic.PROCESO_ACTUALIZACION);
        alerts.addAll(this.alertRepository.findAllByTopicEquals(Types.MessageTopic.PROCESO_INSTALACION));
        return ResponseEntity.ok(alerts);
    }

    @PostMapping(value = "function/call")
    public ResponseEntity callFunction(String device, String function, String param) {
        ApiFunction apiFunction = this.particleService.callFunction(device, function, param);

        if (apiFunction.isOk() || Objects.isNull(apiFunction.getError()))
            return ResponseEntity.ok(apiFunction);

        else return ResponseEntity.unprocessableEntity().body(apiFunction);
    }

    @GetMapping(value = "function/variable")
    public ResponseEntity getVariable(String device, String variable) {
        ApiVariable apiFunction = this.particleService.getVariable(device, variable);
        if (apiFunction.isOk() || Objects.isNull(apiFunction.getError())) return ResponseEntity.ok(apiFunction);
        else return ResponseEntity.unprocessableEntity().body(apiFunction);
    }

    private <T> ResponseEntity<List<T>> mapResponse(List<T> deviceEntities) {
        Response response = new Response();
        if (deviceEntities.isEmpty())
            response = Response.builder().message("NOT DEVICE TYPES FOUND.").content(null).status(Response.HttpStatus.NOT_FOUND).build();
        else response = response.of(deviceEntities);
        return ResponseEntity.status(response.getStatus().code()).body(deviceEntities);
    }
}
