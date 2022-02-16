package com.eaphonetech.common.datatables.mongodb.config;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.eaphonetech.common.datatables.mongodb.repository.EaphoneQueryRepositoryFactoryBean;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

@Configuration
@EnableMongoRepositories(repositoryFactoryBeanClass = EaphoneQueryRepositoryFactoryBean.class, basePackages = {
		"com.eaphonetech.common.datatables.mongodb.repository.model",
		"com.eaphonetech.common.datatables.mongodb.repository"})
public class Config {

	@Bean
	public MongoServer mongoServer() {
		MongoServer server = new MongoServer(new MemoryBackend());
		server.bind();
		return server;
	}

	@Bean
	public MongoClient mongoClient(MongoServer server) {
		return MongoClients
				.create(MongoClientSettings.builder()
						.applyToClusterSettings(
								builder -> builder.hosts(Arrays.asList(new ServerAddress(server.getLocalAddress()))))
						.build());
	}

	@Bean
	@ConditionalOnMissingBean
	public MongoTemplate mongoTemplate(MongoClient mongo) {
		MongoTemplate mt = new MongoTemplate(mongo, "test");
		return mt;
	}
}
