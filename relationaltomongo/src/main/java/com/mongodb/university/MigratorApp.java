package com.mongodb.university;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    private EmployeeRepository employeeRepository;

    @Autowired
    private MongoAsyncWriter mongoAsyncWriter;

    @Autowired
    private EmployeeSqlToMongoMapper employeeSqlToMongoMapper;

    @Autowired
    @Qualifier(MongoAsyncWriter.THREADPOOL_NAME)
    private ThreadPoolTaskExecutor executor;

    @Value("${worker.batchSize}")
    private int batchSize;

    @Value("${worker.drainLevel}")
    private int drainLevel;

    private Instant startTime;
    private int rowNumber;
    private Logger logger = LoggerFactory.getLogger(MigratorApp.class);

    public static void main(String[] args) {
        SpringApplication.run(MigratorApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        this.startTime =  Instant.now();
        this.rowNumber = 0;

        // 1. Open the cursor to the relational database
        final ResultSet resultSet = employeeRepository.buildIterator();

        // 2. Loop the cursor
        try {
            while (resultSet.next()) {

                // 3. Convert from SQL to anything, in this case Document objects
                Document doc = employeeSqlToMongoMapper.sqlToDocument(resultSet);

                // 4. Send to mongodb
                mongoAsyncWriter.write(doc);

                // 5. Optional: could run out of memory when looping cursor. Some batching logic.
                ++rowNumber;
                if (executor.getThreadPoolExecutor().getQueue().size() > batchSize){
                    logger.info("PAUSE.");
                    while (executor.getThreadPoolExecutor().getQueue().size() > drainLevel){
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
