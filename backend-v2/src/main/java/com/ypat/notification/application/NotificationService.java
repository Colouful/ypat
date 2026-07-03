package com.ypat.notification.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * PR-18: notification dispatch (async).
 *
 * Async via Spring's ApplicationEventPublisher. Each
 * notification type is a typed record (Sms / Push / InApp);
 * listeners (@EventListener or @TransactionalEventListener)
 * decide how to deliver them.
 *
 * Why async via Spring events, not RabbitMQ / Kafka:
 *   - Phase 1: same-JVM async is enough. PR-22 final can swap
 *     in an MQ-backed listener without changing this contract.
 *   - Spring events respect @TransactionalEventListener — a
 *     notification fires only after the wallet commit succeeds.
 *     That is what V1.1 §1.2 row 4 ("领域事件 schema 注册") wants
 *     even before we introduce a Schema Registry.
 *
 * Per-channel senders are wired in via Spring beans; PR-18 just
 * ships the contract + the publish-side skeleton.
 */
@Service
public class NotificationService {

    private final ApplicationEventPublisher events;

    public NotificationService(ApplicationEventPublisher events) {
        this.events = events;
    }

    /** Send an SMS verification code. */
    public void sendSms(Sms sms) {
        events.publishEvent(sms);
    }

    /** Push an in-app notification. */
    public void sendPush(Push push) {
        events.publishEvent(push);
    }

    /** In-app message-center entry. */
    public void sendInApp(InApp inApp) {
        events.publishEvent(inApp);
    }

    /** Record. Immutable; carries everything a listener needs. */
    public record Sms(String phoneNumber, String templateId, String[] args) {}
    public record Push(long userId, String title, String body, String deepLink) {}
    public record InApp(long userId, String subject, String body, String iconKey) {}
}