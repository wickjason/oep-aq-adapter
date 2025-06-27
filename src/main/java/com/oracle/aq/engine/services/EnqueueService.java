package com.oracle.aq.engine.services;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.oracle.aq.engine.exceptions.AQAdapterException;
import com.oracle.aq.engine.models.PinEventTy;
import oracle.AQ.AQEnqueueOption;
import oracle.AQ.AQException;
import oracle.AQ.AQMessage;
import oracle.AQ.AQObjectPayload;
import oracle.AQ.AQQueue;

@Service
public class EnqueueService {
	
	private final Logger logger = LoggerFactory.getLogger(EnqueueService.class);	
	
	
	public String enqueue(PinEventTy pinEventTy, AQQueue enqueueQueue, String msgId) throws SQLException, AQAdapterException {
		logger.info("EnqueueService.enqueue(PinEventTy pinEventTy, AQQueue enqueueQueue)");
		
		try {
			System.out.println("EnqueueService.enqueue() "+enqueueQueue.getName());
			System.out.println("EnqueueService.enqueue() "+enqueueQueue.getOwner());
			System.out.println("EnqueueService.enqueue() "+enqueueQueue.getQueueTableName());
		} catch (AQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String responseCode = null;		
	    
	    try {
	    	AQEnqueueOption enqueueOption = new AQEnqueueOption();
	    	AQMessage message = enqueueQueue.createMessage();
	    	//AQMessageProperty messageProperty = new AQMessageProperty();
	    	AQObjectPayload payload = (AQObjectPayload) message.getObjectPayload();
	    	System.out.println("EnqueueService.enqueue() pinEventTy.getSQLTypeName() :-> "+pinEventTy.getSQLTypeName());
	    	pinEventTy.setSqlTypeName("PINOEP.PIN_EVENT_TY");
	    	System.out.println("EnqueueService.enqueue() pinEventTy.getSQLTypeName() after transformation :-> "+pinEventTy.getSQLTypeName());
	    	payload.setPayloadData(pinEventTy); 
	    	message.getMessageProperty().setCorrelation(msgId.toUpperCase());
	    	if (pinEventTy.getLargeFlistBuf() == null) {
	    	    System.out.println("EnqueueService.enqueue() largeFlistBuf is null, making sure it is handled.");
	    	}
	    	//enqueueQueue.startEnqueue();
	    	
	    	enqueueQueue.enqueue(enqueueOption, message); 
	    	responseCode = "SUCCESS";
	    }catch (Exception e) {
			System.out.println("EnqueueService.enqueue Error enqueue : "+e.getMessage());
			responseCode = "FAILUE";
			e.printStackTrace();
	    }		

		return responseCode;
	}
	
	


}