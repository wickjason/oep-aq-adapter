package com.oracle.aq.engine.services;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oracle.aq.engine.exceptions.AQAdapterException;

import oracle.AQ.AQDriverManager;
import oracle.AQ.AQException;
import oracle.AQ.AQQueue;
import oracle.AQ.AQSession;

@Component
public final class AQQueueService implements AQService {

	private static final String AQ_AQORACLE_DRIVER = "oracle.AQ.AQOracleDriver";
	private final Logger logger = LogManager.getLogger(AQQueueService.class);
	// private String aqQueueOwner = "pin";
	// private String aqQueueName = "AQ_QUEUE";
	// private String aqQueueName = "ecs_account";
	// private String aqQueueName = "ecs_account";

	@Override
	public AQQueue getQueue(final Connection connection) throws AQAdapterException {
		try {
			Class.forName(AQ_AQORACLE_DRIVER);
			try {
				logger.info("Trying to create sessin using AQDriverManager." + connection.isClosed());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final AQSession aqSession = AQDriverManager.createAQSession(connection);
			logger.info("AQ Connection for queue is created.");
			// return aqSession.getQueue(aqQueueOwner, aqQueueName);
			return null;
		} catch (ClassNotFoundException | AQException ex) {
			logger.info("Exception is {}", ex.getMessage());
			throw new AQAdapterException("Exception while creating connection with AQ Queue", ex);
		}
	}

	@Override
	public AQQueue getQueue(Connection connection, String owner, String queue) throws AQAdapterException {
		try {
			Class.forName(AQ_AQORACLE_DRIVER);
			try {
				logger.info("Trying to create session using AQDriverManager." + connection.isClosed());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final AQSession aqSession = AQDriverManager.createAQSession(connection);
			logger.info("AQ Connection for queue is created.");
			return aqSession.getQueue(owner, queue);
		} catch (ClassNotFoundException | AQException ex) {
			logger.info("Exception is {}", ex.getMessage());
			throw new AQAdapterException("Exception while creating connection with AQ Queue", ex);
		}
	}
}
