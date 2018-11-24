package com.mongodb.university;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component
@Configuration
@EnableAsync
public class MongoAsyncWriter {

    public static final String DB_NAME = "dev-app";
    public static final String COLL_NAME = "employees";
    public static final Document IDX_EMPNO = new Document().append("emp_no", 1);

    public static final String THREADPOOL_NAME = "threadPoolTaskExecutor";

    private static final int POOL_SIZE = 490;

    private Logger logger = LoggerFactory.getLogger(MongoAsyncWriter.class);

    @Bean(name = THREADPOOL_NAME)
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(POOL_SIZE);
        threadPoolTaskExecutor.setMaxPoolSize(POOL_SIZE);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }

    @Autowired
    private MongoClient mongoClient;

    @PostConstruct
    /**
     * Resets the collection entirely.
     * An example document is inserted to make sure indexes are created as well.
     */
    public void bootstrap(){
        MongoCollection mongoCollection = mongoClient.getDatabase(DB_NAME).getCollection(COLL_NAME);
        mongoCollection.drop();

        try {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("exampleDocument.json");
            String exampleDocument = IOUtils.toString(resourceAsStream, Charset.defaultCharset());
            mongoCollection.insertOne(Document.parse(exampleDocument));
        } catch ( IOException e){
            throw  new IllegalStateException("Unable to bootstrap from example document.", e);
        }

        mongoCollection.createIndex(IDX_EMPNO, new IndexOptions().background(false));
    }

    @Async(THREADPOOL_NAME)
    public void write(Document in){
        MongoCollection mongoCollection = mongoClient.getDatabase(DB_NAME).getCollection(COLL_NAME);

        Document findEmp = (Document) in.get("emp");
        Document titleEmp = (Document) in.get("title");
        Document salaryEmp = (Document) in.get("salary");
        Document deptEmp = (Document) in.get("dept");
        Document managerEmp = (Document) in.get("manager");

        mongoCollection.updateOne(
                new Document().append("emp_no", findEmp.get("emp_no")),
                new Document().append("$setOnInsert", findEmp),
                new UpdateOptions().upsert(true)
        );

        mongoCollection.updateOne(
                Filters.eq("emp_no", findEmp.get("emp_no")),
                Updates.combine(
                        Updates.addToSet("salaries", salaryEmp),
                        Updates.addToSet("titles", titleEmp),
                        Updates.addToSet("departments", deptEmp),
                        Updates.addToSet("managing", managerEmp)
                ));

    }
}
