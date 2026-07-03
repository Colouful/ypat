package com.ypat.notification.application;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * PR-18: async listener skeleton.
 *
 * The actual SMS / push / IM sender integrations (Tencent
 * Cloud SMS, JPush / Getui, the in-app message store) live
 * with the per-channel sender beans and are wired in here.
 * PR-18 ships only the listener shape; real integrations land
 * with PR-18 follow-up.
 *
 * Each @EventListener method runs on Spring's task executor,
 * not the request thread. The webhook / worker that handles
 * the deliver (Tencent SMS, etc.) is decoupled from the HTTP
 * request lifecycle.
 */
@Component
public class AsyncNotificationListener {

    @Async
    @EventListener
    public void onSms(NotificationService.Sms sms) {
        // PR-18 follow-up: route to Tencent SMS SDK.
        // For now the event is dropped after this method returns.
    }

    @Async
    @EventListener
    public void onPush(NotificationService.Push push) {
        // PR-18 follow-up: route to JPush / Getui / FCM.
    }

    @Async
    @EventListener
    public void onInApp(NotificationService.InApp inApp) {
        // PR-18 follow-up: write to t_mess_info table (Flyway-managed).
    }
}