package com.oracle.aq.engine.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.oracle.aq.engine.exceptions.AQAdapterException;

import oracle.AQ.AQQueue;

public interface AQService {
	
	String ADD_SUBSCRIBER_SQL = "BEGIN DBMS_AQADM.ADD_SUBSCRIBER(?, SYS.AQ$_AGENT(?, NULL, NULL)); END;";
    String REMOVE_SUBSCRIBER_SQL = "BEGIN DBMS_AQADM.REMOVE_SUBSCRIBER(?, SYS.AQ$_AGENT(?, NULL, NULL)); END;";

	AQQueue getQueue(Connection connection) throws AQAdapterException;
	
	AQQueue getQueue(Connection connection, String owner, String queue) throws AQAdapterException;
	
	default void addSubscriber(Connection connection, String queueName, String subscriber) throws SQLException {
        try (PreparedStatement addStmt = connection.prepareStatement(ADD_SUBSCRIBER_SQL)) {
            addStmt.setString(1, queueName);
            addStmt.setString(2, subscriber);
            addStmt.executeUpdate();
        }
    }

    default void removeSubscriber(Connection connection, String queueName, String subscriber) throws SQLException {
        try (PreparedStatement removeStmt = connection.prepareStatement(REMOVE_SUBSCRIBER_SQL)) {
            removeStmt.setString(1, queueName);
            removeStmt.setString(2, subscriber);
            removeStmt.executeUpdate();
        }
    }
}
