package com.oracle.aq.engine.operations;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.oracle.aq.engine.clients.AQAdapterClientBrm;
import jakarta.annotation.PreDestroy;

@Service
public class AQOperationsBrm {

	private static final Logger LOGGER = LoggerFactory.getLogger(AQOperationsBrm.class);

	@Value("${oracle.brm.aq.queue.dequeue.name}")
	private String queueName;

	private final AQAdapterClientBrm aqAdapterClient;
	private final ExecutorService executorservice;

	public AQOperationsBrm(final AQAdapterClientBrm aqAdapterClient,
			@Qualifier("taskPoolBrm") final ExecutorService executorService) {
		this.aqAdapterClient = aqAdapterClient;
		this.executorservice = executorService;
	}

	public void getMessage() {
		LOGGER.info("AQOperationsBrm.getMessage() invoked at := {}", new Date());
		LOGGER.info("AQOperationsBrm.getMessage() queue name is := {}", queueName);
		executorservice.execute(aqAdapterClient);
	}

	@PreDestroy
	public void gracefulShutdown() {
		LOGGER.info("AQOperationsOic Graceful shutdown initiated...");
		
		// Step 1: Stop polling first
		stopAQListeners();
		
		//  Step 2: Shut down the executor
		stopExecService(executorservice);
		
		// Step 3: Close DB resources
		try {
	        aqAdapterClient.stopAQListeners(); // Ensures DB connections are closed
	    } catch (Exception e) {
	        LOGGER.error("Error while closing DB connections during shutdown", e);
	    }
	}	
	
	public void stopAQListeners() {
		LOGGER.info("AQOperationsBrm Stopping Listeners");
		aqAdapterClient.close();
	}

	private void stopExecService(final ExecutorService executorService) {
		LOGGER.info("AQOperationsBrm stopping executor service...");
		try {
			executorservice.shutdown();			
		} catch (Exception ex) {
			LOGGER.error("Error while closing connection");
		}
		//executorService.shutdown();
		try {
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				LOGGER.info("AQOperationsBrm Calling shutdownNow");
				executorService.shutdownNow();
			}
		} catch (InterruptedException ex) {
			executorService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		LOGGER.info("AQOperationsBrm executor service stopped successfully...");
	}

}
