package com.oracle.aq.engine.adapters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.oracle.aq.engine.exceptions.AQAdapterException;
import com.oracle.aq.engine.models.PinEventTy;
import com.oracle.aq.engine.services.AQAdapterUtils;
import com.oracle.aq.engine.services.EnqueueService;

import lombok.extern.slf4j.Slf4j;
import oracle.AQ.AQDequeueOption;
import oracle.AQ.AQException;
import oracle.AQ.AQMessage;
import oracle.AQ.AQQueue;

@Slf4j
public abstract class AQBaseAdapterOic {

	@Value("${oracle.brm.aq.queue.enqueue.name}")
	private String queueBrm;
	@Value("${oracle.oep.aq.queue.dequeue.name}")
	private String queueOep;
	@Value("${oracle.brm.aq.queue.owner}")
	private String ownerBrm;
	@Value("${oracle.oep.aq.queue.owner}")
	private String ownerOep;
	@Value("${dequeueOption.brm.setCondition}")
	private String brmDequeCondition;


	@Autowired
	private EnqueueService enqueueService;

	private final Logger logger = LoggerFactory.getLogger(AQBaseAdapterOic.class);
	public boolean stopPolling;

	protected abstract Connection getConnectionBrm() throws SQLException;
	protected abstract Connection getConnectionOep() throws SQLException;

	protected abstract AQQueue getQueue() throws SQLException, AQAdapterException, AQException;
	protected abstract AQQueue getQueue(Connection connection, String owner, String queue) throws SQLException, AQAdapterException, AQException;
	protected abstract AQQueue getQueueForEnqueue(Connection connection, String owner, String queue) throws SQLException, AQAdapterException, AQException;

	protected abstract void sendMessage(String payload) throws AQAdapterException;

	protected void execute() throws SQLException, AQAdapterException {
		System.out.println("AQBaseAdapterOic.execute()");

		AQQueue queue = null;
		AQQueue enqueueQueue = null;
		Connection brmConnection = getConnectionBrm();
		Connection connectionOep = getConnectionOep();

		brmConnection.setAutoCommit(false);
		connectionOep.setAutoCommit(false);
		final AQDequeueOption dequeueOption = new AQDequeueOption();

		try {
			queue = getQueue(brmConnection, ownerBrm, queueBrm);
			queue.start();
			enqueueQueue = getQueueForEnqueue(connectionOep, ownerOep, queueOep);
			dequeueOption.setDequeueMode(AQDequeueOption.DEQUEUE_REMOVE);
			//dequeueOption.setCondition("TAB.CORRID = 'MyDataNotification'");
			//dequeueOption.setCondition("TAB.CORRID = brmDequeCondition");
			log.info("Brm Deque Condition = {}" ,brmDequeCondition);
			//dequeueOption.setCondition("TAB.CORRID = " + brmDequeCondition);
			dequeueOption.setCondition("TAB.CORRID = '" + brmDequeCondition + "'");
		} catch (AQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		System.out.println("AQBaseAdapterOic.execute() Started Listening on queue: " + queueBrm);

		String messageId = null;
		String correlationId = null;


		while (!stopPolling) {
			AQMessage message = null;			
			
			try {
				message = queue.dequeue(dequeueOption, PinEventTy.class);				
			} catch (AQException e) {
				logger.error("AQBaseAdapterOic: Dequeue attempt {} failed. Retrying...", e.getMessage());
			}		

			if (message == null) {
				continue; // Skip processing if message is null
			}

			try {
				byte[] messageIdFromMessage = message.getMessageId();
				messageId = HexFormat.of().formatHex(messageIdFromMessage);
				correlationId = message.getMessageProperty().getCorrelation();

				logger.info("AQBaseAdapterOic: Processing messageId: {} with correlationId: {}", messageId.toUpperCase(), correlationId);

				PinEventTy pinEventTy = (PinEventTy) message.getObjectPayload().getPayloadData();
				String payload = AQAdapterUtils.getPayload(pinEventTy);
				logger.info("AQBaseAdapterOic payload contains := {}", payload);

				if (payload == null) {
					logger.error("Payload is empty. MessageId:- {}", message.getMessageId());
				} else {
					// Enqueue to OEP queue
					enqueueService.enqueue(pinEventTy, enqueueQueue, messageId);
					logger.info("AQBaseAdapterOic: messageId: {} with correlationId: {}", messageId.toUpperCase(), correlationId, " enqueued successfully");
					logger.info("AQBaseAdapterOic: attempting OEP commit for enqueued message");
					connectionOep.commit();
					logger.info("AQBaseAdapterOic: OEP commit successfull for enqueued message");
				}

				logger.info("AQBaseAdapterOic: attempting BRM commit for dequeued message");
				brmConnection.commit();
				logger.info("AQBaseAdapterOic: BRM commit for successfull for dequeued message");
			} catch (Exception ex) {
				logger.error("AQBaseAdapterOic: Exception while processing message", ex);
				logger.info("Rolling back both BRM and OEP transactions");
				brmConnection.rollback();
				connectionOep.rollback();
				logger.info("Roll back of BRM and OEP transactions successfull");
				
			}
		}
	}



	public void close()  {
		logger.info("AQBaseAdapterOic.close()");
		stopPolling = true;
	}

}