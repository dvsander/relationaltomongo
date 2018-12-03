package com.mongodb.university;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public static final Document IDX_EMPNO = new Document().append("emp_no", 1);
    public static final String THREADPOOL_NAME = "threadPoolTaskExecutor";

    private Logger logger = LoggerFactory.getLogger(MongoAsyncWriter.class);

    @Bean(name = THREADPOOL_NAME)
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return threadPoolTaskExecutor;
    }

    @Autowired
    private MongoClient mongoClient;

    @Value("${mongo.database}")
    private String mongoDatabaseName;

    @Value("${mongo.collection}")
    private String mongoCollectionName;

    @PostConstruct
    /**
     * Resets the collection entirely.
     * An example document is inserted to make sure indexes are created as well.
     */
    public void bootstrap(){
        MongoCollection mongoCollection = mongoClient.getDatabase(mongoDatabaseName).getCollection(mongoCollectionName);
        mongoCollection.drop();

        try {
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("exampleDocument.json");
            String exampleDocument = IOUtils.toString(resourceAsStream, Charset.defaultCharset());
            mongoCollection.insertOne(Document.parse(exampleDocument));
        } catch ( IOException e){
            throw  new IllegalStateException("Unable to bootstrap from example document.", e);
        }

        mongoCollection.createIndex(IDX_EMPNO, new IndexOptions().background(false).unique(true));
    }

    @Async(THREADPOOL_NAME)
    public void write(Document in){
        MongoCollection mongoCollection = mongoClient.getDatabase(mongoDatabaseName).getCollection(mongoCollectionName);

        Document findEmp = (Document) in.get("emp");
        Document titleEmp = (Document) in.get("title");
        Document salaryEmp = (Document) in.get("salary");
        Document deptEmp = (Document) in.get("dept");
        Document managerEmp = (Document) in.get("manager");

        mongoCollection.updateOne(
                new Document("emp_no", findEmp.get("emp_no")),
                Updates.combine(
                        Updates.setOnInsert("emp_no", findEmp.get("emp_no")),
                        Updates.setOnInsert("birth_date", findEmp.get("birth_date")),
                        Updates.setOnInsert("first_name", findEmp.get("first_name")),
                        Updates.setOnInsert("last_name", findEmp.get("last_name")),
                        Updates.setOnInsert("gender", findEmp.get("gender")),
                        Updates.setOnInsert("hire_date", findEmp.get("hire_date")),
                        Updates.addToSet("salaries", salaryEmp),
                        Updates.addToSet("titles", titleEmp),
                        Updates.addToSet("departments", deptEmp),
                        Updates.addToSet("managing", managerEmp)
                ),
                new UpdateOptions().upsert(true)
        );

    }
}
