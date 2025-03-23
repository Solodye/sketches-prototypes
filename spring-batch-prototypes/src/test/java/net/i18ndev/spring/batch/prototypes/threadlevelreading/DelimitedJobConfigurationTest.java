package net.i18ndev.spring.batch.prototypes.threadlevelreading;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(locations = {"/simple-job-launcher-context.xml" })
class DelimitedJobConfigurationTest {

    @Test
    public void testLaunchJobWithJavaConfig() throws Exception {
        // given
        ApplicationContext context = new AnnotationConfigApplicationContext(DelimitedJobConfiguration.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", "net/i18ndev/spring/batch/prototypes/threadlevelreading/data/delimited.csv")
                .addString("outputFile", "file:./target/test-outputs/delimitedOutput.csv")
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }

}