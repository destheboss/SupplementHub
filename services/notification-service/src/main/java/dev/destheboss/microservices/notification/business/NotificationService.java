package dev.destheboss.microservices.notification.business;

import dev.destheboss.microservices.order.event.OrderPlacedEvent;
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

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        log.info("Received message from order-placed topic: {}", orderPlacedEvent);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("supplementhub@email.com");
            messageHelper.setTo(orderPlacedEvent.getEmail());
            messageHelper.setSubject(String.format("Your order with OrderNumber %s was placed successfully", orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                            Hi,
                                                
                            Your order with order number %s was placed successfully.
                                                
                            Kind regards,
                            SupplementHub Team
                            """
                    , orderPlacedEvent.getOrderNumber()
            ));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("Order Notification email sent to {}", orderPlacedEvent.getEmail());
        } catch (MailException e) {
            log.error("Failed to send email to {}", orderPlacedEvent.getEmail(), e);
            throw new RuntimeException("Failed to send email");
        }
    }
}
