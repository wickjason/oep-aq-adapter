package com.oracle.aq.engine.services;

import com.oracle.aq.engine.clients.OepCoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.oracle.aq.engine.models.Response;

@Service
public class OepServiceImpl implements OepService {
	@Autowired
    private OepCoreClient oepCoreClient;
	
	private final Logger logger = LoggerFactory.getLogger(OepServiceImpl.class);

	@Override
	public Response executeOepService(String payload) {
		logger.info("OepServiceImpl.executeOepService() payload:-> {}", payload);
		Response response = null;
		try {
			response = oepCoreClient.invprocessmydata(payload);
			if (response.getCode().equals("0")) {
				response.setCode("200");
			} else if (response.getCode().equals("1")) {
				response.setCode("400");
			} else {
				response.setCode("500");
				response.setDescription("Internal Server Error!!");
			}
		}catch (Exception e){
			logger.info("Error occured while calling OEP api : " + e.getMessage());
			response = new Response();
			response.setCode("500");
		}
		return response;
	}
}
