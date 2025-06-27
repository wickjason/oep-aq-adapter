package com.oracle.aq.engine.configs;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import oracle.jdbc.pool.OracleDataSource;

@Configuration
public class AQAdapterDatabseConfig {

	
	 // BRM Specific DB Properties
	 @Value("${oracle.brm.datasource.jdbc-url}")
	 private String url;

	 @Value("${oracle.brm.datasource.username}")
	 private String userName;

	 @Value("${oracle.brm.datasource.password}")
	 private String password;
	 
	 // OEP Specific DB Properties
	 @Value("${oracle.oep.datasource.jdbc-url}")
	 private String oepUrl;

	 @Value("${oracle.oep.datasource.username}")
	 private String oepUserName;

	 @Value("${oracle.oep.datasource.password}")
	 private String oepPassword;

	  
	
	@Bean(name = "taskPoolBrm")
	public ExecutorService fixedThreadPoolBrm() {
		return Executors.newSingleThreadExecutor();
	}
	
	@Bean(name = "taskPoolOic")
	public ExecutorService fixedThreadPoolOic() {
		return Executors.newSingleThreadExecutor();
	}
	
	@Bean(name = "taskPoolOep")
	public ExecutorService fixedThreadPoolOep() {
		return Executors.newSingleThreadExecutor();
	}
	
	 @Bean(name = "dataSourceBrm")
	  public DataSource dataSource() throws SQLException {
		 System.out.println("Inside AQAdapterConfig");
		 //System.out.println("Inside AQAdapterConfig url :" +url);
		 //System.out.println("Inside AQAdapterConfig userName :" +userName);
		 //System.out.println("Inside AQAdapterConfig password :" +password);
		 final OracleDataSource ds = new OracleDataSource();
		    ds.setUser(userName);
		    ds.setPassword(password);
		    ds.setURL(url);
		    ds.setImplicitCachingEnabled(true);
		 
			/*
			 * DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
			 * dataSourceBuilder.driverClassName("oracle.jdbc.driver.OracleDriver");
			 * dataSourceBuilder.url("jdbc:oracle:thin:@localhost:1521:orcl");
			 * dataSourceBuilder.username("telia_pcrf");
			 * dataSourceBuilder.password("asap#456");
			 * return dataSourceBuilder.build()
			 */
        return ds;
	  }
	 
	 @Bean(name = "dataSourceOep")
	  public DataSource dataSourceOep() throws SQLException {
		 System.out.println("Inside AQAdapterConfig");
		 //System.out.println("Inside AQAdapterConfig url :" +url);
		 //System.out.println("Inside AQAdapterConfig userName :" +userName);
		 //System.out.println("Inside AQAdapterConfig password :" +password);
		 final OracleDataSource ds = new OracleDataSource();
		    ds.setUser(oepUserName);
		    ds.setPassword(oepPassword);
		    ds.setURL(oepUrl);
		    ds.setImplicitCachingEnabled(true);
		 
			/*
			 * DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
			 * dataSourceBuilder.driverClassName("oracle.jdbc.driver.OracleDriver");
			 * dataSourceBuilder.url("jdbc:oracle:thin:@localhost:1521:orcl");
			 * dataSourceBuilder.username("telia_pcrf");
			 * dataSourceBuilder.password("asap#456");
			 * return dataSourceBuilder.build()
			 */
       return ds;
	  }
}
