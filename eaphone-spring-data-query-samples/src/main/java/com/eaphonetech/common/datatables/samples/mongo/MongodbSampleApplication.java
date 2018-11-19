package com.eaphonetech.common.datatables.samples.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.eaphonetech.common.datatables.mongodb.repository.EaphoneQueryRepositoryFactoryBean;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

@SpringBootApplication
@EnableMongoRepositories(repositoryFactoryBeanClass = EaphoneQueryRepositoryFactoryBean.class)
public class MongodbSampleApplication {

    @Configuration
    static class Config {
        @Bean
        MongoServer mongoServer() {
            MongoServer server = new MongoServer(new MemoryBackend());
            server.bind();
            return server;
        }

        @Bean
        MongoClient mongoClient(MongoServer server) {
            return new MongoClient(new ServerAddress(server.getLocalAddress()));
        }

        @Bean
        MongoTemplate mongoTemplate(MongoClient mongo) {
            MongoTemplate mt = new MongoTemplate(mongo, "test");
            return mt;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(MongodbSampleApplication.class, args);
    }

}
