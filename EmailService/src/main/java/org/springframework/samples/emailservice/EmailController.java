package org.springframework.samples.emailservice;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.emailservice.model.EmailData;
import org.springframework.samples.emailservice.model.Owner;
import org.springframework.samples.emailservice.model.OwnerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmailController {

	Logger logger = LogManager.getLogger(EmailController.class);

	@Autowired
	private OwnerRepository ownerRepository;

	@PostMapping("/registerEmail")
	public String registerEmail(@RequestBody OwnerDTO owner) {
		logger.info("owner email : {} owner first name : {} last name : {}",
			owner.getEmail(), owner.getFirstName(), owner.getLastName());
		return owner.getFirstName();
	}


	@GetMapping("/getOwners")
	public List<Owner> getOwners() {
		return ownerRepository.findAll();
	}

}
