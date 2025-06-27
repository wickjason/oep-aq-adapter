package com.oracle.aq.engine.clients;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.oracle.aq.engine.models.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OepCoreClient {
	@Value("${oep.core.url}")
	String oepCoreUrl;
	@Value("${oep.accountmgmt.url}")
	String accountMgmtUrl;
	@Autowired
	private RestTemplate restTemplate;
	public String generateNotification(String request, String jmsCorrelationID) {
		//Calling order entry core
		log.info("OepCoreClient.generateNotification() request: {}",request);
		log.info("OepCoreClient.generateNotification started");
		HttpHeaders headers = new HttpHeaders();
		//MediaType mediaType = new MediaType("application", "xml", StandardCharsets.UTF_8);
		//headers.setContentType(mediaType);
		headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
		//  headers.add(HttpHeaders.CONTENT_TYPE, "text/xml; charset=UTF-8");
		HttpEntity<String> requestEntity = new HttpEntity<>(request, headers);
		//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		ResponseEntity<String> notificationResponse = null;
		String response;
		try {
			notificationResponse = restTemplate
					.postForEntity(oepCoreUrl + "/Notification/"+jmsCorrelationID,
							requestEntity,
							String.class);
			response = notificationResponse.getBody();
		} catch (RestClientException e) {
			System.out.println("Oep core client-Rest client exception occured");
			log.error("Oep core client-Rest client exception occured");
			response = "FAILED :- "+e.getMessage();

		}
		catch (Exception e) {
			System.out.println("Oep core client-exception occured");
			log.error("Oep core client-Exception occured");
			response = "FAILED :- " + e.getMessage();
		}
		log.info("OepCoreClient.generateNotification returned");
		return response;
	}
	public Response invprocessmydata(String request) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
		HttpEntity<String> requestEntity = new HttpEntity<>(request, headers);
		//restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		ResponseEntity<Response> accountMgmtResponse = restTemplate
				.postForEntity(accountMgmtUrl ,
						requestEntity,
						Response.class);
		log.info("Oep Response Received: " + accountMgmtResponse.getBody());
		return accountMgmtResponse.getBody();
	}
}