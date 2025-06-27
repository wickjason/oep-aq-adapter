package com.oracle.aq.engine.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.oracle.aq.engine.operations.AQOperationsOep;


@Component
public class StartupEventTriggerOep implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private AQOperationsOep aqOperations;

	private static final Logger LOGGER = LoggerFactory.getLogger(StartupEventTriggerOep.class);
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		LOGGER.info("AQAdapter OEP application has started.");
		aqOperations.getMessage();
	}
	
	
}