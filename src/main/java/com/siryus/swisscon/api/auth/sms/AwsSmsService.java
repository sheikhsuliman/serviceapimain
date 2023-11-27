package com.siryus.swisscon.api.auth.sms;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.siryus.swisscon.api.auth.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AwsSmsService implements SmsService {

    private AmazonSNS sns;

    @Autowired
    public void setSnsClient(AmazonSNS sns) {
        this.sns = sns;
    }

    @Override
    public void sendSmsMessage(String message, String phoneNumber) {
        try {
            PublishResult result = sns.publish(new PublishRequest()
                    .withMessage(message)
                    .withPhoneNumber(phoneNumber));
            logSmsMessage(message, phoneNumber);
            validatePublishResult(result, phoneNumber);
        } catch (InvalidParameterException e) {
            throw AuthException.smsSendFailed(phoneNumber);
        }
    }

    private void validatePublishResult(PublishResult result, String phoneNumber) {
        if (HttpStatus.OK.value() != result.getSdkHttpMetadata().getHttpStatusCode()) {
            throw AuthException.smsSendFailed(phoneNumber);
        }
    }

    private void logSmsMessage(String message, String phoneNumber) {
        log.info("SMS sent to " + phoneNumber + ":");
        log.info(message);
    }

}
