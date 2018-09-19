package com.eaphonetech.common.datatables.samples.mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.eaphonetech.common.datatables.mongodb.repository.EaphoneQueryRepositoryFactoryBean;
import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;

@SpringBootApplication
@EnableMongoRepositories(repositoryFactoryBeanClass = EaphoneQueryRepositoryFactoryBean.class)
public class MongodbSampleApplication {

    @Configuration
    static class Config {
        @Bean
        Fongo fongo() {
            return new Fongo("InMemoryMongo");
        }

        @Bean
        MongoClient mongoClient(Fongo fongo) {
            return fongo.getMongo();
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
