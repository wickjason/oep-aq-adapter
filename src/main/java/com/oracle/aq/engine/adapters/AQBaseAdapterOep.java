package com.oracle.aq.engine.adapters;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.oracle.aq.engine.exceptions.AQAdapterException;
import com.oracle.aq.engine.models.PinEventTy;
import com.oracle.aq.engine.models.Response;
import com.oracle.aq.engine.services.AQAdapterUtils;
import com.oracle.aq.engine.services.OepService;

import lombok.extern.slf4j.Slf4j;
import oracle.AQ.AQDequeueOption;
import oracle.AQ.AQEnqueueOption;
import oracle.AQ.AQException;
import oracle.AQ.AQMessage;
import oracle.AQ.AQObjectPayload;
import oracle.AQ.AQQueue;


@Slf4j
public abstract class AQBaseAdapterOep {
	@Value("${oracle.oep.aq.queue.enqueue.name}")
	private String queueOep;
	@Value("${oracle.oep.aq.queue.owner}")
	private String ownerOep;
	@Value("${dequeueOption.oic.setCondition}")
	private String oicDequeCondition;
	@Autowired
	private OepService oepService;
	private String podName;
	private final Logger logger = LoggerFactory.getLogger(AQBaseAdapterOep.class);
	public boolean stopPolling;
	protected abstract Connection getConnection() throws SQLException;
	protected abstract AQQueue getQueue() throws SQLException, AQAdapterException, AQException;
	protected abstract AQQueue getQueue(Connection connection, String owner, String queue)
			throws SQLException, AQAdapterException, AQException;
	protected abstract void sendMessage(String payload) throws AQAdapterException;
	protected void execute() throws SQLException, AQAdapterException {
		System.out.println("AQBaseAdapterOep.execute()");
		System.out.println("AQBaseAdapterOep.execute() stopPolling = " + stopPolling);
		if (podName == null) {
			// Retrieve and log the pod name only when execute is called
			podName = System.getenv("POD_NAME");
			if (podName == null || podName.isEmpty()) {
				logger.warn("Pod name is not set. Using default value.");
				podName = "default-pod-name"; // Fallback value if the environment variable is not available
			}
			logger.info("Pod Name populated in execute method: {}", podName);
		}
		/* doing changes -- starts */
		AQQueue queue = null;
		Connection connection = getConnection();
		connection.setAutoCommit(false);
		final AQDequeueOption dequeueOption = new AQDequeueOption();
		try {
			queue = getQueue(connection, ownerOep, queueOep);
			queue.start();
			dequeueOption.setDequeueMode(AQDequeueOption.DEQUEUE_REMOVE);
			//dequeueOption.setCondition("TAB.CORRID = 'MyDataNotificationResponse'");
			log.info("Oic Deque Condition = {}" ,oicDequeCondition);
			dequeueOption.setCondition("TAB.CORRID = '" + oicDequeCondition + "'");
		} catch (AQException e) {
			logger.error("AQBaseAdapterOep.execute() Exception setting dequeueOption. Stopping polling.");
			e.printStackTrace();
		}
		
		String payload;
		PinEventTy pinEventTy;
		System.out.println("AQBaseAdapterOep.execute() Started Listening on queue : " + queueOep);
		while (!stopPolling) {
			
			AQMessage message = null;
			
			try {
				message = queue.dequeue(dequeueOption, PinEventTy.class);				
			} catch (AQException e) {
				logger.error("AQBaseAdapterOep: Dequeue attempt {} failed. Retrying...", e.getMessage());
			}

			if (message == null) {
				continue; // Skip processing if message is null
			}
			try {
				pinEventTy = (PinEventTy) message.getObjectPayload().getPayloadData();
				payload = AQAdapterUtils.getPayload(pinEventTy);
				logger.info("AQBaseAdapterOep payload contains := {}", payload);
				if (payload == null) {
					logger.error("AQBaseAdapterOep Payload is empty. MessageId:-" + message.getMessageId());
				} else {
					// REST CALL -- RestTemplate
					// oepCoreClient(payload);
					// BRM OpCode Call needed
					log.info("Invoking Accountmgmt-oep Api");
					Response response = oepService.executeOepService(payload);
					if (null != response && !"200".equals(response.getCode())) {
						// do enqueue
						System.out.println("AQBaseAdapterOep.execute() OP CODE CALL FAILED.. Enqueue message back..");
						enqueue(pinEventTy, queue, response);
					} else {
						System.out.println("AQBaseAdapterOep.execute() OP CODE CALL SUCCESSFUL.. Nothing to do..");
					}
				}
				log.info("AQBaseAdapterOep: attempting to commit on OEP DB");
				connection.commit();
				log.info("AQBaseAdapterOep: commit success on OEP DB");
			} catch (Exception ex) {
				logger.error("AQBaseAdapterOep Exception while processing message", ex);
				connection.rollback();
				log.info("AQBaseAdapterOep: rollback success on OEP DB");

				throw new AQAdapterException(ex);
			}
		}
	}
	public String enqueue(PinEventTy pinEventTy, AQQueue enqueueQueue, Response response)
			throws SQLException, AQAdapterException {
		logger.info("AQBaseAdapterOep.enqueue()");
		String responseCode = null;
		String eventName = response.getCode().equals("400") ? "MANUAL_REVIEW" + "_" + response.getDescription()
				: "BRM_SERVICE_UNREACHABLE"; // 
		
		try {
			AQEnqueueOption enqueueOption = new AQEnqueueOption();
			AQMessage message = enqueueQueue.createMessage();
			// AQMessageProperty messageProperty = new AQMessageProperty();
			AQObjectPayload payload = (AQObjectPayload) message.getObjectPayload();
			// payload.setPayloadData(pinEventTy2);
			System.out
					.println("EnqueueService.enqueue() pinEventTy.getSQLTypeName() :-> " + pinEventTy.getSQLTypeName());
			pinEventTy.setSqlTypeName(ownerOep+".PIN_EVENT_TY");
			System.out.println("EnqueueService.enqueue() pinEventTy.getSQLTypeName() after transformation :-> "
					+ pinEventTy.getSQLTypeName());
			payload.setPayloadData(pinEventTy);
			message.getMessageProperty().setCorrelation(eventName);
			if (pinEventTy.getLargeFlistBuf() == null) {
				System.out.println("EnqueueService.enqueue() largeFlistBuf is null, making sure it is handled.");
			}
			enqueueQueue.enqueue(enqueueOption, message);
			responseCode = "SUCCESS";
		} catch (Exception e) {
			System.out.println("OepDequeueStrategy.enqueue Error enqueue : " + e.getMessage());
			e.printStackTrace();
			responseCode = "FAILURE";
		}
		return responseCode;
	}
	public void close()  {
		logger.info("AQBaseAdapterOep.close()");
		stopPolling = true;
	}
}