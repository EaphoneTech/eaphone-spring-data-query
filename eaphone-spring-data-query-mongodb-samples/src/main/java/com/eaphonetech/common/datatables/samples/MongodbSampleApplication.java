package com.eaphonetech.common.datatables.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.eaphonetech.common.datatables.mongodb.repository.DataTablesRepositoryFactoryBean;

@SpringBootApplication
@EnableMongoRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class MongodbSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongodbSampleApplication.class, args);
    }

}
