//package lettrage.example.lettrage.config;
//
//import lettrage.example.lettrage.model.Order;
//import lettrage.example.lettrage.repository.OrderRepository;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.data.RepositoryItemWriter;
//import org.springframework.batch.item.support.ListItemReader;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.util.List;
//
//@Configuration
//@EnableBatchProcessing
//public class BatchConfig {
//
//    @Bean
//    public Job saveOrdersJob(JobRepository jobRepository, Step saveOrdersStep) {
//        return new JobBuilder("saveOrdersJob", jobRepository)
//                .start(saveOrdersStep)
//                .build();
//    }
//
//    @Bean
//    public Step saveOrdersStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
//                               ListItemReader<Order> orderReader, RepositoryItemWriter<Order> orderWriter) {
//        return new StepBuilder("saveOrdersStep", jobRepository)
//                .<Order, Order>chunk(1000, transactionManager) // Process in chunks of 1000 orders
//                .reader(orderReader)
//                .writer(orderWriter)
//                .build();
//    }
//
//    @Bean
//    public ListItemReader<Order> orderReader(List<Order> orders) {
//        return new ListItemReader<>(orders);
//    }
//
//    @Bean
//    public RepositoryItemWriter<Order> orderWriter(OrderRepository orderRepository) {
//        RepositoryItemWriter<Order> writer = new RepositoryItemWriter<>();
//        writer.setRepository(orderRepository);
//        writer.setMethodName("save");
//        return writer;
//    }
//}