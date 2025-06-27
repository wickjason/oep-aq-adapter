package com.oracle.aq.engine.services;

import com.oracle.aq.engine.models.Response;

public interface OepService {
	
	public Response executeOepService(String payload);
}