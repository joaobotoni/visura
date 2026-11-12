package com.visura.service;

import com.visura.database.entity.Device;
import com.visura.database.entity.User;
import com.visura.database.repository.DeviceRepository;
import com.visura.dto.request.DeviceRequest;
import com.visura.exception.custom.DeviceException;
import com.visura.exception.enums.problem.DeviceProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Device enroll(User user, DeviceRequest request) {
        upsert(user, request);
        return find(user, request.identifier());
    }

    private void upsert(User user, DeviceRequest request) {
        deviceRepository.upsert(user.getId(), request.identifier(), platform(request),
                request.manufacturer(), request.model(), request.osVersion());
    }

    private static String platform(DeviceRequest request) {
        return request.platform().getValue();
    }

    private Device find(User user, UUID identifier) {
        return deviceRepository.findByUserAndIdentifier(user, identifier)
                .orElseThrow(() -> new DeviceException(DeviceProblem.NOT_FOUND));
    }
}