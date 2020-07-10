package com.nationwide.hackathon.cr.email;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("notificationPlugin")
@PropertySource("classpath:notification.properties")
public class NotificationPlugin {

	@Autowired
	private Environment env;

	static Logger _LOGGER = LoggerFactory.getLogger(NotificationPlugin.class);
	
	static Properties properties = new Properties();
	static {
		properties.setProperty("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.smtp.socketFactory.fallback", "false");
		properties.setProperty("mail.smtp.port", "465");
		properties.setProperty("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.debug", "true");
		properties.put("mail.store.protocol", "pop3");
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.debug.auth", "true");
		properties.setProperty("mail.pop3.socketFactory.fallback", "false");
	}

	public boolean sendEmail(String email, String toEmail) {
		properties.setProperty("mail.smtp.host",
				env.getProperty("mail.host.name"));
		Session session = Session.getDefaultInstance(properties,
				new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(env
								.getProperty("mail.from.address"), env
								.getProperty("mail.from.password"));
					}
				});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(env
					.getProperty("mail.from.address")));
			message.setRecipients(MimeMessage.RecipientType.TO,
					InternetAddress.parse(toEmail));
			message.setSubject(env.getProperty("mail.email.subject"));
			message.setContent(email, "text/html; charset=utf-8");
			Transport.send(message);
			_LOGGER.info("Email sent  " + email);
		} catch (MessagingException e) {
			_LOGGER.error("Error while sending email "
					+ e.getLocalizedMessage());
			return false;
		}
		return true;

	}
	

}

