package com.oracle.aq.engine.adapters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.oracle.aq.engine.clients.OepCoreClient;
import com.oracle.aq.engine.exceptions.AQAdapterException;
import com.oracle.aq.engine.models.PinEventTy;
import com.oracle.aq.engine.services.AQAdapterUtils;

import lombok.extern.slf4j.Slf4j;
import oracle.AQ.AQDequeueOption;
import oracle.AQ.AQException;
import oracle.AQ.AQMessage;
import oracle.AQ.AQQueue;

@Slf4j
public abstract class AQBaseAdapterBrm {

	@Autowired
	private OepCoreClient oepCoreClient;

	@Value("${oracle.brm.aq.queue.dequeue.name}")
	private String queueBrm;
	@Value("${oracle.brm.aq.queue.owner}")
	private String ownerBrm;

	private final Logger logger = LoggerFactory.getLogger(AQBaseAdapterBrm.class);
	public boolean stopPolling;

	protected abstract Connection getConnection() throws SQLException;

	protected abstract AQQueue getQueue() throws SQLException, AQAdapterException, AQException;

	protected abstract AQQueue getQueue(Connection connection, String owner, String queue)
			throws SQLException, AQAdapterException, AQException;

	protected abstract void sendMessage(String payload) throws AQAdapterException;

	protected void execute() throws SQLException, AQAdapterException {
		System.out.println("AQBaseAdapterBrm.execute()");
		System.out.println("AQBaseAdapterBrm.execute() stopPolling = " + stopPolling);
		/* doing changes -- starts */
		AQQueue queue = null;
		Connection connection = getConnection();
		connection.setAutoCommit(false);
		final AQDequeueOption dequeueOption = new AQDequeueOption();
		try {
			queue = getQueue(connection, ownerBrm, queueBrm);
			queue.start();
			System.out.println("AQBaseAdapterBrm.execute() Started Listening on queue : " + queueBrm);
			// final AQDequeueOption dequeueOption = new AQDequeueOption();
			dequeueOption.setDequeueMode(AQDequeueOption.DEQUEUE_REMOVE);
			dequeueOption.setCondition("TAB.CORRID = 'NotificationRequest'");
		} catch (AQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String messageId = null;
		String correlationId = null;

		while (!stopPolling) {

			AQMessage message = null;

			try {
				message = queue.dequeue(dequeueOption, PinEventTy.class);
			} catch (AQException e) {
				logger.error("AQBaseAdapterBrm: Dequeue attempt {} failed. Retrying...", e.getMessage());
			}

			if (message == null) {
				continue; // Skip processing if message is null
			}

			try {
				byte[] messageIdFromMessage = message.getMessageId();
				messageId = HexFormat.of().formatHex(messageIdFromMessage);
				correlationId = message.getMessageProperty().getCorrelation();

				System.out.println("AQBaseAdapterBrm.execute() messageId: " + messageId.toUpperCase());
				System.out.println("AQBaseAdapterBrm.execute() correlationId: " + correlationId);

				PinEventTy pinEventTy = (PinEventTy) message.getObjectPayload().getPayloadData();
				String payload = AQAdapterUtils.getPayload(pinEventTy);
				logger.info("AQBaseAdapterBrm payload contains := {}", payload);

				if (payload == null) {
					logger.error("Payload is empty. MessageId:-" + message.getMessageId());
				} else {
					// Enqueue to OEP queue
					// enqueueService.enqueue(pinEventTy, messageId);
					String response = oepCoreClient.generateNotification(payload,"BRM request notification");
					log.info("AqadapterBRM-NotificationResponse {}", response);
					if (null!= response && response.contains("FAILED")) {
						logger.warn("AQBaseAdapterBrm.execute() NotificationResponse Call Failed. Logging to AQ_EVENT_FAILURES");
					}else{
						log.info("NotificationResponse {}", response);
					}
				}

				log.info("AQBaseAdapterBrm: attempting commit on BRM DB");

				connection.commit();
				log.info("AQBaseAdapterBrm: commit success on BRM DB");
			} catch (Exception ex) {
				logger.error("Exception while processing message", ex);	
				connection.rollback();
				log.info("AQBaseAdapterBrm: rollback successful on BRM DB");
				throw new AQAdapterException(ex);
			}
		}
	}

	public void close() {
		logger.info("AQBaseAdapterBrm Stopping adapter.");
		stopPolling = true;
	}

	public void setStopPolling(boolean stopPolling) {
		this.stopPolling = stopPolling;
	}

	public void stopPolling() {
		stopPolling = true;
		logger.info("AQBaseAdapterBrm Polling stopped ");
	}

}
