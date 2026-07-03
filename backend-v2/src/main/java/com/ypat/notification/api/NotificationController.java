package com.ypat.notification.api;

import com.ypat.notification.application.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * PR-18: thin notification controller.
 *
 * Routes:
 *   POST /api/notification/sms  - send SMS verification code
 *   POST /api/notification/push - push in-app notification
 *
 * Both endpoints just publish a Spring event. The actual
 * delivery is async via AsyncNotificationListener.
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/sms")
    public ResponseEntity<?> sendSms(@RequestBody Map<String, Object> body) {
        String phone = (String) body.get("phone");
        String templateId = (String) body.get("templateId");
        @SuppressWarnings("unchecked")
        String[] args = ((java.util.List<String>) body.getOrDefault("args", java.util.List.of()))
                .toArray(new String[0]);
        service.sendSms(new NotificationService.Sms(phone, templateId, args));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/push")
    public ResponseEntity<?> sendPush(@RequestBody Map<String, Object> body) {
        long userId = ((Number) body.get("userId")).longValue();
        String title = (String) body.get("title");
        String bodyText = (String) body.get("body");
        String deepLink = (String) body.getOrDefault("deepLink", "");
        service.sendPush(new NotificationService.Push(userId, title, bodyText, deepLink));
        return ResponseEntity.accepted().build();
    }
}