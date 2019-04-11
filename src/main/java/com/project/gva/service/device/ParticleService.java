package com.project.gva.service.device;

import com.project.gva.entity.DeviceEntity;
import com.project.gva.entity.InstallationEntity;
import com.project.gva.entity.ReadDevice;
import com.project.gva.entity.UpdateEntity;
import com.project.gva.model.ApiDevice;
import com.project.gva.model.ApiFunction;
import com.project.gva.model.ApiVariable;
import com.project.gva.model.Types;

import java.io.File;
import java.util.List;

public interface ParticleService {

    List<ApiDevice> getAllDevices();

    List<ReadDevice> getAllDevicesStatus();

    Types.DeviceStatus getDeviceStatus(String device);

    DefaultParticleService.ApiInstalationMessage installVersion(String device, File file);

    InstallationEntity execInstall(InstallationEntity installation, DeviceEntity deviceEntity, File file, UpdateEntity update, boolean valid);

    ApiFunction callFunction(String device, String function, String param);

    ApiVariable getVariable(String device, String variable);

    int claimDevice(String id);

    int unClaimDevice(String id);
}
