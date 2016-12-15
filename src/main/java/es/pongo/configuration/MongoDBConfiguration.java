package es.pongo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableSpringDataWebSupport
@EnableMongoRepositories(basePackages = "es.pongo.repository")
public class MongoDBConfiguration extends AbstractMongoConfiguration{
	
	@Value("${mongodb.database}")
	private String database;
	
	@Value("${mongodb.host}")
	private String host;

	@Value("${mongodb.port}")
	private int port;

	@Override
	protected String getDatabaseName() {
		return database;
	}

	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient(host, port);
	}
}
