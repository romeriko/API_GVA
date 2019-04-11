package com.project.gva;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class GvaApplication {

    @Value(value = "${app.sms.sid}")
    private String SMS_SID;

    @Value(value = "${app.sms.token}")
    private String SMS_ACCESS_TOKEN;

    public static void main(String[] args) {
        SpringApplication.run(GvaApplication.class, args);
    }

    @PostConstruct
    public void init() {
        Twilio.init(SMS_SID, SMS_ACCESS_TOKEN);
    }
}
