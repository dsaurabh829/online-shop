package com.dsaurabh.notificationservice;

import com.dsaurabh.notificationservice.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Payload;

@SpringBootApplication
@Slf4j
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Autowired
	private JavaMailSender mailSender;

	@KafkaListener(topics = {"notification_topic"}, groupId = "orderNotification")
	public void consumeOrderEvents(@Payload ConsumerRecord<String, OrderEvent> record){

		log.info("mesaage received ----------> " + record.value());

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo("cu.16bcs1188@gmail.com");
		message.setFrom("dsaurabh829@gmail.com");
		message.setSubject("Order Summary");
		message.setText(record.value().toString());

		mailSender.send(message);

	}

}
