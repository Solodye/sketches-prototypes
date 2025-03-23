package net.i18ndev.spring.batch.prototypes.threadlevelreading;

import net.i18ndev.spring.batch.common.DataSourceConfiguration;
import net.i18ndev.spring.batch.dao.CustomerCredit;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
@Import(DataSourceConfiguration.class)
@ComponentScan
public class DelimitedJobConfiguration {

	@Bean
	@StepScope
	public FlatFileItemReader<CustomerCredit> itemReader(@Value("#{jobParameters[inputFile]}") Resource resource) {
		BeanWrapperFieldSetMapper<CustomerCredit> bfs = new BeanWrapperFieldSetMapper<>();
		bfs.setTargetType(CustomerCredit.class);
		DefaultLineMapper<CustomerCredit> dl = new DefaultLineMapper<>();
		dl.setFieldSetMapper(bfs);
		dl.setLineTokenizer(getLineTokenizer());
		return new FlatFileItemReaderBuilder<CustomerCredit>().name("itemReader")
			.resource(resource)
			.lineMapper(dl)
			.build();
	}

	public DelimitedLineTokenizer getLineTokenizer(){
		DelimitedLineTokenizer dlt =  new DelimitedLineTokenizer();
		dlt.setNames("name", "credit");
		return dlt;
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<CustomerCredit> itemWriter(
			@Value("#{jobParameters[outputFile]}") WritableResource resource) {
		return new FlatFileItemWriterBuilder<CustomerCredit>().name("itemWriter")
			.resource(resource)
			.delimited()
			.names("name", "credit")
			.build();
	}

	@Bean
	public ThreadPoolTaskExecutor getJobTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(2);
		taskExecutor.setMaxPoolSize(2);
		taskExecutor.setQueueCapacity(2);
		taskExecutor.setThreadNamePrefix("ProcessJob-");
		taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
		taskExecutor.afterPropertiesSet();
		taskExecutor.initialize();
		return taskExecutor;
	}
	@Bean
	public Job job(JobRepository jobRepository, JdbcTransactionManager transactionManager,
			ItemReader<CustomerCredit> itemReader, ItemWriter<CustomerCredit> itemWriter) {
		return new JobBuilder("ioSampleJob", jobRepository)
			.start(new StepBuilder("step1", jobRepository)
				.<CustomerCredit, CustomerCredit>chunk(2, transactionManager)
					.listener(new DebugChunkListener())
				.reader(itemReader)
				.processor(new CustomerCreditIncreaseProcessor())
				.writer(itemWriter)
					.taskExecutor(getJobTaskExecutor())
					.throttleLimit(2)
				.build())
			.build();
	}

}
