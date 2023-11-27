package com.siryus.swisscon.api.auth.sms;


public interface SmsService {
    void sendSmsMessage(String message, String phoneNumber);
}
