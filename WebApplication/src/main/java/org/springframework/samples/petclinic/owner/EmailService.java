package org.springframework.samples.petclinic.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Value("${email.service.address}")
	private String serviceAddress;

	public void sendEmail(Owner owner) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.postForEntity(serviceAddress, owner, String.class);
		}
		catch (Exception e) {
			log.warn("Failed to send email: {}", e.getMessage());
		}
	}

}
