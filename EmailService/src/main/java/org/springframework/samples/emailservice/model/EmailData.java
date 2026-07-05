package org.springframework.samples.emailservice.model;


import java.io.Serializable;

/**
 * email data value object
 */
public class EmailData implements Serializable {

	private String emailAddress;
	private String subject;
	private String body;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "EmailData{" +
			"body='" + body + '\'' +
			'}';
	}
}
