package com.eaphonetech.common.datatables.samples.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.eaphonetech.common.datatables.jpa.repository.DataTablesRepositoryFactoryBean;

@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class JpaSampleApplication {
    @Configuration
    static class Config {
    }

    public static void main(String[] args) {
        SpringApplication.run(JpaSampleApplication.class, args);
    }

}
