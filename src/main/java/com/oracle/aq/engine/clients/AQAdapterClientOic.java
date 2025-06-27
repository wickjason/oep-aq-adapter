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

import com.oracle.aq.engine.adapters.AQBaseAdapterOic;
import com.oracle.aq.engine.exceptions.AQAdapterException;
import com.oracle.aq.engine.services.AQService;

import oracle.AQ.AQException;
import oracle.AQ.AQQueue;

@Component
public class AQAdapterClientOic extends AQBaseAdapterOic implements Runnable {

	private Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	@Qualifier("dataSourceBrm") 
	private DataSource brmDataSource;
	
	@Autowired
	@Qualifier("dataSourceOep") 
	private DataSource oepDataSource;

	private AQQueue queue;
	
	private AQQueue queueForEnqueue;

	
	@Autowired
	private AQService service;

	@Override
	public void run() {
		LOGGER.info("AQAdapterClientOic.run() thread executed at time {} ", new Date());
		try {
			execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AQAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		LOGGER.info("AQAdapterClientOic.run() thread finished execution time {} ", new Date());
	}

	
	@Override
	protected void sendMessage(final String payload) throws AQAdapterException {
		LOGGER.info("Send event notification to OEP := {} ", payload);
	}

	@Override
	protected AQQueue getQueue() throws SQLException, AQAdapterException, AQException {
		if (queue == null) {
			//queue = service.getQueue(getConnection());			
		}
		System.out.println("AQAdapterClientOic.getQueue() queue.getName() :-> " +queue.getName());
		return queue;
	}

	
	
	
	public void stopAQListeners() {
		LOGGER.info("Stopping AQAdapterClientOic");		
		try {
			closeResources();
		} catch (SQLException e) {
			LOGGER.error("AQAdapterClientOic: Error while closing DB connections during shutdown", e);
		}
		
	}
	
	private void closeResources() throws SQLException {
		LOGGER.info("Closing AQAdapterClientOic resources...");
		
		if (brmDataSource != null) {
	        try (Connection connection = brmDataSource.getConnection()) {
	            if (!connection.isClosed()) {
	                connection.close();
	            }
	        }
	    }
		
		if (oepDataSource != null) {
	        try (Connection connection = oepDataSource.getConnection()) {
	            if (!connection.isClosed()) {
	                connection.close();
	            }
	        }
	    }		
		LOGGER.info("AQAdapterClientOic All database connections closed successfully.");
	}

	@Override
	protected AQQueue getQueue(Connection Connection, String owner, String queuee) throws SQLException, AQAdapterException, AQException {
		System.out.println("AQAdapterClientOic.getQueue()");
		System.out.println("AQAdapterClientOic.getQueue() Connection.getMetaData().getURL() :-> "+Connection.getMetaData().getURL());
		System.out.println("AQAdapterClientOic.getQueue() queuee :-> "+queuee);
		System.out.println("AQAdapterClientOic.getQueue() owner :-> "+owner);
		if (queue == null) {
			queue = service.getQueue(Connection, owner, queuee);			
		}else {
			System.out.println("AQAdapterClientOic.getQueue() QUEUE is NOT NULL");
		}
		System.out.println("AQAdapterClientOic.getQueue() queue.getName() :-> " +queue.getName());
		return queue;
	}

	@Override
	protected Connection getConnectionBrm() throws SQLException {
		return brmDataSource.getConnection();
	}

	@Override
	protected Connection getConnectionOep() throws SQLException {
		return oepDataSource.getConnection();
	}


	@Override
	protected AQQueue getQueueForEnqueue(Connection connection, String owner, String queuee)
			throws SQLException, AQAdapterException, AQException {
		System.out.println("AQAdapterClientOic.getQueueForEnqueue()");
		System.out.println("AQAdapterClientOic.getQueueForEnqueue() Connection.getMetaData().getURL() :-> "+connection.getMetaData().getURL());
		System.out.println("AQAdapterClientOic.getQueueForEnqueue() queuee :-> "+queuee);
		System.out.println("AQAdapterClientOic.getQueueForEnqueue() owner :-> "+owner);
		if (queueForEnqueue == null) {
			queueForEnqueue = service.getQueue(connection, owner, queuee);			
		}else {
			System.out.println("AQAdapterClientOic.getQueueForEnqueue() queueForEnqueue is NOT NULL");
		}
		System.out.println("AQAdapterClientOic.getQueueForEnqueue() queue.getName() :-> " +queueForEnqueue.getName());
		return queueForEnqueue;
	}

}
