package com.project.gva.service.device;

import com.condos.shared.http.C2ApiResponse;
import com.condos.shared.http.C2Header;
import com.condos.shared.http.C2HttpClient;
import com.google.gson.reflect.TypeToken;
import com.project.gva.entity.DeviceEntity;
import com.project.gva.entity.InstallationEntity;
import com.project.gva.entity.ReadDevice;
import com.project.gva.entity.UpdateEntity;
import com.project.gva.model.ApiDevice;
import com.project.gva.model.ApiFunction;
import com.project.gva.model.ApiVariable;
import com.project.gva.model.Types;
import com.project.gva.repository.DeviceRepository;
import com.project.gva.repository.InstallationRepository;
import com.project.gva.service.message.MessageService;
import lombok.Data;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

@Service(value = "defaultParticleService")
public class DefaultParticleService implements ParticleService {

    private final
    String authorization;

    private final
    C2HttpClient httpClient;

    private final
    InstallationRepository installationRepository;

    private final
    DeviceRepository deviceRepository;

    private final
    MessageService messageService;

    @Value(value = "${app.sms.receiver}")
    private String SMS_RECEIVER;

    @Autowired
    public DefaultParticleService(@Value(value = "${particle.api}") String api,
                                  @Value(value = "${particle.accessToken}") String at, InstallationRepository installationRepository, DeviceRepository deviceRepository, MessageService messageService) throws IllegalAccessException {
        this.authorization = "Bearer ".concat(at);
        this.httpClient = new C2HttpClient(api);
        this.installationRepository = installationRepository;
        this.deviceRepository = deviceRepository;
        this.messageService = messageService;
    }


    @Override
    public List<ApiDevice> getAllDevices() {
        Type listType = new TypeToken<ArrayList<ApiDevice>>() {
        }.getType();
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        Response response = httpClient.get("devices", header);
        return C2ApiResponse.getBody(response, listType);
    }

    @Override
    public List<ReadDevice> getAllDevicesStatus() {
        Type listType = new TypeToken<ArrayList<ApiDevice>>() {
        }.getType();
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        Response response = httpClient.get("devices", header);
        List<ApiDevice> devices = C2ApiResponse.getBody(response, listType);
        devices.forEach((device) -> device.setStatus(getDeviceStatus(device.getId())));

        List<ReadDevice> readDevices = new ArrayList<>();
        devices.forEach(device -> {
            DeviceEntity entity = this.deviceRepository.findById(device.getId()).orElse(null);
            readDevices.add(
                    ReadDevice.builder()
                            .date(new Date()).name(entity == null ? device.getName() : entity.getName()).logDevice(device.getId()).status(device.getStatus()).build());
        });

        return readDevices;
    }

    @Override
    public Types.DeviceStatus getDeviceStatus(String device) {
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        Response response = httpClient.put("devices/".concat(device).concat("/ping"), null, header);
        ApiStatus status = C2ApiResponse.getBody(response, ApiStatus.class);
        if (status.isOnline())
            return Types.DeviceStatus.ONLINE;
        return Types.DeviceStatus.OFFLINE;
    }

    @Override
    public ApiInstalationMessage installVersion(String device, File file) {
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();

        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file_type", "binary")
                .addFormDataPart("file", file.getName(), RequestBody.create(mediaType, file))
                .build();

        Response response = httpClient.put("devices/".concat(device), body, header);

        return C2ApiResponse.getBody(response, ApiInstalationMessage.class);
    }

    @Override
    public InstallationEntity execInstall(InstallationEntity installation, DeviceEntity deviceEntity, File file, UpdateEntity update, boolean valid) {

        DefaultParticleService.ApiInstalationMessage message = this.installVersion(deviceEntity.getId(), file);

        if (message.startUpdate()) {
            InstallationEntity newInstall;
            if (Objects.nonNull(installation)) {
                installation.setCurrentlyInstalled(false);
                this.installationRepository.saveAndFlush(installation);
                newInstall = InstallationEntity.builder()
                        .device(deviceEntity)
                        .currentlyInstalled(true)
                        .firstUpdated(installation.getFirstUpdated())
                        .firstVersion(installation.getFirstVersion())
                        .id(UUID.randomUUID().toString())
                        .lastUpdated(new Date())
                        .update(update)
                        .valid(valid)
                        .validation(update.getValidation())
                        .build();

                deviceEntity.setVersion(installation.getUpdate().getName().concat(" - v").concat(installation.getUpdate().getVersion()));
            } else {
                newInstall = InstallationEntity.builder()
                        .device(deviceEntity)
                        .currentlyInstalled(true)
                        .firstUpdated(new Date())
                        .firstVersion(update)
                        .id(UUID.randomUUID().toString())
                        .lastUpdated(new Date())
                        .update(update)
                        .valid(valid)
                        .validation(update.getValidation())
                        .build();
            }
            deviceEntity.setLastUpdate(new Date());
            this.deviceRepository.save(deviceEntity);
            String msg = String.format("El dispositivo: %s, se actualizó a la versión: %s", deviceEntity.getName(), update.getVersion());
            messageService.sendSms(SMS_RECEIVER, msg, deviceEntity.getId(), Types.MessageTopic.PROCESO_INSTALACION, Types.MessageStatus.SUCCESS, newInstall.getId());

            return this.installationRepository.save(newInstall);
        } else {
            String msg = String.format("Actualización: %s - V%s, pendiente, para el dispositivo : %s", update.getName(), update.getVersion(), deviceEntity.getName());
            messageService.sendSms(SMS_RECEIVER, msg, deviceEntity.getId(), Types.MessageTopic.PROCESO_INSTALACION, Types.MessageStatus.ERROR, null);
            return null;
        }
    }

    @Override
    public ApiFunction callFunction(String device, String function, String param) {
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        RequestBody body;

        if (param == null) {
            body = new FormBody.Builder().build();
        } else body = new FormBody.Builder()
                .add("args", param)
                .build();

        Response response = httpClient.post(String.format("devices/%s/%s", device, function), body, header);
        return C2ApiResponse.getBody(response, ApiFunction.class);
    }

    @Override
    public ApiVariable getVariable(String device, String variable) {
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        Response response = httpClient.get(String.format("devices/%s/%s", device, variable), header);
        return C2ApiResponse.getBody(response, ApiVariable.class);
    }

    @Override
    public int claimDevice(String id) {
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        RequestBody body = new FormBody.Builder().add("id", id).build();
        Response response = httpClient.post("devices", body, header);
        return response.code();
    }

    @Override
    public int unClaimDevice(String id) {
        C2Header header = C2Header.builder().authorization(authorization).contentType("application/json").build();
        RequestBody body = new FormBody.Builder().add("params", id).build();
        Response response = httpClient.delete(String.format("devices/%s", id), body, header);
        return response.code();
    }

    @Data
    private static class ApiStatus {
        private boolean online;
        private boolean ok;
    }

    @Data
    public static class ApiInstalationMessage {
        private String id;
        private String status;
        private String error;

        public boolean isOk() {
            return error == null;
        }

        public boolean startUpdate() {
            return "Update started".equals(this.status);
        }
    }


}
