package com.project.gva.service.message;

import com.project.gva.entity.AlertEntity;
import com.project.gva.model.Types;
import com.project.gva.repository.AlertRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class DefaultMessageService implements MessageService {

    @Value(value = "${app.sms.phone}")
    private String TWILIO_NUMBER;

    private final
    AlertRepository alertRepository;

    @Autowired
    public DefaultMessageService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public void sendSms(String to, String message, String device, Types.MessageTopic topic, Types.MessageStatus status, String document) {

        if (Objects.isNull(this.alertRepository.findByMessageEquals(message))) {

            AlertEntity alert = AlertEntity
                    .builder()
                    .id(UUID.randomUUID().toString())
                    .document(document)
                    .device(device)
                    .date(new Date())
                    .status(status)
                    .topic(topic)
                    .message(message)
                    .build();

            this.alertRepository.save(alert);

            Message.creator(new PhoneNumber(to), new PhoneNumber(TWILIO_NUMBER), message).create();
        }
    }
}
