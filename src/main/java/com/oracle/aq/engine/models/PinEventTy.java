package com.oracle.aq.engine.models;

import java.sql.Clob;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;

public class PinEventTy implements SQLData {
	private String eventName;
	private String message;
	private String flistBuf;
	private Clob largeFlistBuf;

	private String sqlTypeName;

	public String getSqlTypeName() {
		return sqlTypeName;
	}

	public void setSqlTypeName(String sqlTypeName) {
		this.sqlTypeName = sqlTypeName;
	}

	@Override
	public String getSQLTypeName() {
		return sqlTypeName;
	}

	@Override
	public void readSQL(final SQLInput stream, final String typeName) throws SQLException {
		System.out.println("PinEventTy.readSQL() called");
		sqlTypeName = typeName;
		System.out.println("PinEventTy.readSQL() sqlTypeName := " + sqlTypeName);
		message = stream.readString();
		System.out.println("PinEventTy.readSQL() message is >> " + message);
		flistBuf = stream.readString();
		System.out.println("PinEventTy.readSQL() flistBuf is >> " + flistBuf);
		
		System.out.println("PinEventTy.readSQL() Exiting");
	}

	@Override
	public void writeSQL(final SQLOutput stream) throws SQLException {
		System.out.println("PinEventTy.writeSQL() called");
		// Write non-CLOB fields
		//stream.writeString(name);
	    stream.writeString(message);
	    stream.writeString(flistBuf);

	    // Handle largeFlistBuf (CLOB) carefully
	    if (largeFlistBuf != null) {
	        // Only write CLOB if it's not null
	        stream.writeClob(largeFlistBuf);
	        System.out.println("PinEventTy.writeSQL() largeFlistBuf is written");
	    } else {
	        // Handle null CLOB
	        stream.writeClob(null); // Explicitly write null for the CLOB field
	        System.out.println("PinEventTy.writeSQL() largeFlistBuf is null, writing null.");
	    }
	    
		//throw new SQLException("Implementation not defined");
	}

	public String getName() {
		return eventName;
	}

	public String getFlistBuf() {
		return flistBuf;
	}

	public Clob getLargeFlistBuf() {
		return largeFlistBuf;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}
