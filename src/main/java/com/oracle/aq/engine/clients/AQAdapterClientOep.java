package com.oracle.aq.engine.clients;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.oracle.aq.engine.adapters.AQBaseAdapterOep;
import com.oracle.aq.engine.exceptions.AQAdapterException;
import com.oracle.aq.engine.services.AQService;

import oracle.AQ.AQException;
import oracle.AQ.AQQueue;

@Component
public class AQAdapterClientOep extends AQBaseAdapterOep implements Runnable {

	private Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("dataSourceOep")
	private DataSource dataSource;

	private AQQueue queue;

	@Autowired
	private AQService service;

	@Override
	public void run() {
		LOGGER.info("AQAdapterClientOep.run() thread executed at time {} ", new Date());
		try {
			execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AQAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOGGER.info("AQAdapterClientOep.run() thread finished execution time {} ", new Date());
	}

	@Override
	protected Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	protected void sendMessage(final String payload) throws AQAdapterException {
		LOGGER.info("Send event notification to OEP := {} ", payload);
	}

	@Override
	protected AQQueue getQueue() throws SQLException, AQAdapterException, AQException {
		if (queue == null) {
			queue = service.getQueue(getConnection());
		}
		System.out.println("AQAdapterClientOep.getQueue() queue.getName() :-> " + queue.getName());
		return queue;
	}
	
	public void stopAQListeners() {
		LOGGER.info("AQAdapterClientOep: Stopping AQAdapterClientOic");		
		try {
			closeResources();
		} catch (SQLException e) {
			LOGGER.error("AQAdapterClientOep: Error while closing DB connections during shutdown", e);
		}
	}
	
	private void closeResources() throws SQLException {
		LOGGER.info("Closing AQAdapterClientOep resources...");
		
		if (dataSource != null) {
	        try (Connection connection = dataSource.getConnection()) {
	            if (!connection.isClosed()) {
	                connection.close();
	            }
	        }
	    }
		
		LOGGER.info("AQAdapterClientOep All database connections closed successfully.");
	}

	@Override
	protected AQQueue getQueue(Connection connection, String owner, String queuee)
			throws SQLException, AQAdapterException, AQException {
		System.out.println("AQAdapterClientOep.getQueue(Connection connection, String owner, String queuee)");
		if (queue == null) {
			queue = service.getQueue(connection, owner, queuee);
		}
		System.out.println("AQAdapterClientOep.getQueue() queue.getName() :-> " + queue.getName());
		return queue;
	}

}
