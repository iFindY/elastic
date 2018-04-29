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
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
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
  public KafkaConsumerSearch kafkaConsumerSearch(MessageService messageService) {

    return new KafkaConsumerSearch(messageService);
  }

  @Bean
  public MessageService messageService(MessageRepository messageRepository,
                                       KafkaProducerResult kafkaProducer) {

    return new MessageServiceImpl(messageRepository, kafkaProducer);
  }

  @Bean
  public RestTemplate restTemplate() {

    return new RestTemplate();
  }
}
