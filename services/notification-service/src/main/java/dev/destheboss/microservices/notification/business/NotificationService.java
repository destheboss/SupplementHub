package dev.destheboss.microservices.notification.business;

import dev.destheboss.microservices.order.event.OrderCancelledEvent;
import dev.destheboss.microservices.order.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-confirmed")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent: {}", event);

        sendEmail(
                event.getEmail(),
                String.format("Your order %s was confirmed", event.getOrderNumber()),
                String.format("""
                        Hi,

                        Your order with order number %s was confirmed successfully.

                        Kind regards,
                        SupplementHub Team
                        """, event.getOrderNumber())
        );
    }

    @KafkaListener(topics = "order-cancelled")
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent: {}", event);

        sendEmail(
                event.getEmail(),
                String.format("Your order %s was cancelled", event.getOrderNumber()),
                String.format("""
                        Hi,

                        Unfortunately your order with order number %s was cancelled.
                        Reason: %s

                        Kind regards,
                        SupplementHub Team
                        """, event.getOrderNumber(), event.getReason())
        );
    }

    private void sendEmail(String to, String subject, String body) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("supplementhub@email.com");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(body);
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Email sent to {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email");
        }
    }
}
