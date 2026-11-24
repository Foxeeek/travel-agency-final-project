package com.epam.finaltask.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityEventLogger {
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event){
        log.info("User '{}' successfully authenticated", event.getAuthentication().getName());
}

    @EventListener
    public void onAccountDisabled(AuthenticationFailureDisabledEvent event){
        log.warn("Login attempt by blocked user '{}'", event.getAuthentication().getName());
    }

    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event){
        log.warn("Login failed for user '{}': bad credentials",event.getAuthentication().getName());
    }



}