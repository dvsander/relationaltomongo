package com.mongodb.university;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@EnableScheduling
public class MigratorApp implements CommandLineRunner{

    @Autowired
    private EmployeeSqlCursor employeeSqlCursor;

    @Autowired
    private EmployeeSqlToMongoMapper employeeSqlToMongoMapper;

    @Autowired
    private MongoAsyncWriter mongoAsyncWriter;

    @Autowired
    @Qualifier(MongoAsyncWriter.THREADPOOL_NAME)
    private ThreadPoolTaskExecutor executor;

    private Logger logger = LoggerFactory.getLogger(MigratorApp.class);

    public static void main(String[] args) {
        SpringApplication.run(MigratorApp.class, args);
    }

    private static final int BATCH_SIZE = 100000;
    private static final int DRAIN_LEVEL = 1000;

    private Instant startTime;
    private int rowNumber;

    @Override
    public void run(String... args) throws Exception {

        this.startTime =  Instant.now();
        this.rowNumber = 0;

        // 1. Open the cursor to the relational database
        final ResultSet resultSet = employeeSqlCursor.buildIterator();

        // 2. Loop the cursor
        try {
            while (resultSet.next()) {

                // 3. Convert from SQL to anything, in this case Document objects
                Document doc = employeeSqlToMongoMapper.sqlToDocument(resultSet);

                // 4. Send to mongodb
                mongoAsyncWriter.write(doc);

                ++rowNumber;

                // 5. Optional: could run out of memory when looping cursor. Some batching logic.
                if (executor.getThreadPoolExecutor().getQueue().size() > BATCH_SIZE){
                    logger.info("PAUSE.", BATCH_SIZE);
                    while (executor.getThreadPoolExecutor().getQueue().size() > DRAIN_LEVEL){
                        // hold the door
                    }
                    logger.info("RESUMING...");
                }
            }
            resultSet.close();
        } catch (SQLException e){
            throw new RuntimeException("Error getting results", e);
        } finally {
            executor.shutdown();
        }

    }

    @Scheduled(fixedRate = 2000)
    public void statusMonitoring() {
        if (startTime == null)
            return;

        Duration duration = Duration.between(startTime, Instant.now());
        ThreadPoolExecutor monitor = executor.getThreadPoolExecutor();

        long completedTaskCount = monitor.getCompletedTaskCount();

        logger.info("{} SQL {} rows read ({}/s) - MDB {} rows processed ({}/s, {} queued)",
                duration, rowNumber,  Math.floor(rowNumber/duration.getSeconds()),
                completedTaskCount, Math.floor(completedTaskCount/duration.getSeconds()), monitor.getQueue().size());

    }

}
