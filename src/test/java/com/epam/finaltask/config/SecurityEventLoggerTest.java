package com.epam.finaltask.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SecurityEventLoggerTest {

    private final SecurityEventLogger eventLogger = new SecurityEventLogger();
    private final Logger logger = (Logger) LoggerFactory.getLogger(SecurityEventLogger.class);
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        logger.detachAppender(appender);
    }

    private Authentication auth() {
        return new UsernamePasswordAuthenticationToken("john", "secret-password");
    }

    @Test
    void onAuthenticationSuccess_LogsInfoWithUsername() {
        eventLogger.onAuthenticationSuccess(new AuthenticationSuccessEvent(auth()));

        assertEquals(1, appender.list.size());
        assertEquals(Level.INFO, appender.list.get(0).getLevel());
        assertEquals("User 'john' successfully authenticated", appender.list.get(0).getFormattedMessage());
    }

    @Test
    void onBadCredentials_LogsWarnWithoutPassword() {
        eventLogger.onBadCredentials(new AuthenticationFailureBadCredentialsEvent(auth(), new BadCredentialsException("bad")));

        assertEquals(1, appender.list.size());
        assertEquals(Level.WARN, appender.list.get(0).getLevel());
        assertEquals("Login failed for user 'john': bad credentials", appender.list.get(0).getFormattedMessage());
        assertFalse(appender.list.get(0).getFormattedMessage().contains("secret-password"));
    }

    @Test
    void onAccountDisabled_LogsWarnForBlockedUser() {
        eventLogger.onAccountDisabled(new AuthenticationFailureDisabledEvent(auth(), new DisabledException("off")));

        assertEquals(1, appender.list.size());
        assertEquals(Level.WARN, appender.list.get(0).getLevel());
        assertEquals("Login attempt by blocked user 'john'", appender.list.get(0).getFormattedMessage());
    }
}
