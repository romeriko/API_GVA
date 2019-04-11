package com.project.gva.service.message;

import com.project.gva.model.Types;

public interface MessageService {

    void sendSms(String to, String message, String device, Types.MessageTopic topic, Types.MessageStatus status, String document);
}
