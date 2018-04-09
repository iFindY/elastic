package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.elasticsearch.service.MessageServiceImpl;
import de.arkadi.elasticsearch.kafka.KafkaConsumerDelete;
import de.arkadi.elasticsearch.kafka.KafkaConsumerSave;
import de.arkadi.elasticsearch.kafka.KafkaConsumerSearch;
import de.arkadi.elasticsearch.kafka.KafkaProducerResult;
import de.arkadi.elasticsearch.model.ResultDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
@EnableAutoConfiguration
@PropertySource("classpath:/application.properties")
@Import({ KafkaConfig.class, ElasticsearchConfig.class })
public class AppConfig {

  @Bean
  public KafkaProducerResult kafkaProducer(KafkaTemplate<String, ResultDTO> kafkaTemplate,
                                           @Value("${kafka.out.cassandra.topic}")
                                             String kafkaOutTopic) {

    return new KafkaProducerResult(kafkaTemplate, kafkaOutTopic);
  }

  @Bean
  public KafkaConsumerSave kafkaConsumerSave(MessageService messageService) {

    return new KafkaConsumerSave(messageService);
  }

  @Bean
  public KafkaConsumerDelete kafkaConsumerDelete(MessageService messageService) {

    return new KafkaConsumerDelete(messageService);
  }

  @Bean
  public KafkaConsumerSearch kafkaConsumerSearch(MessageService messageService,
                                                 KafkaProducerResult kafkaProducerResult) {

    return new KafkaConsumerSearch(messageService, kafkaProducerResult);
  }

  @Bean
  public MessageService messageService(MessageRepository messageRepository,
                                       KafkaProducerResult kafkaProducer) {

    return new MessageServiceImpl(messageRepository, kafkaProducer);
  }
}
