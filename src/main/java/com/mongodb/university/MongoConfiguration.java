package com.mongodb.university;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoClient mongo() {
        return new MongoClient( "localhost" , 27017 );
    }
}
