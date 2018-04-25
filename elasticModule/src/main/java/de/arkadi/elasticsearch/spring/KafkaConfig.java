package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.model.DeleteDTO;
import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@PropertySource("classpath:/application.properties")
public class KafkaConfig {

  @Value("${kafka.bootstrap-servers}")
  private String bootstrap;
  @Value("${kafka.consumer.group.id}")
  private String groupId;

  @Bean
  public Map<String, Object> producerConfigs() {

    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public Map<String, Object> consumerConfig() {

    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    return props;
  }

  @Bean
  public ConsumerFactory<String, SaveDTO> consumerFactorySave() {

    return new DefaultKafkaConsumerFactory<>(consumerConfig(),
                                             new StringDeserializer(),
                                             new JsonDeserializer<>(SaveDTO.class));
  }

  @Bean
  public ConsumerFactory<String, RequestDTO> consumerFactoryRequest() {

    return new DefaultKafkaConsumerFactory<>(consumerConfig(),
                                             new StringDeserializer(),
                                             new JsonDeserializer<>(RequestDTO.class));
  }

  @Bean
  public ConsumerFactory<String, DeleteDTO> consumerFactoryDelete() {

    return new DefaultKafkaConsumerFactory<>(consumerConfig(),
                                             new StringDeserializer(),
                                             new JsonDeserializer<>(DeleteDTO.class));
  }

  @Bean
  public ProducerFactory<String, ResultDTO> producerFactory() {

    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, ResultDTO> kafkaTemplate() {

    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, SaveDTO> kafkaListenerContainerFactorySave() {

    ConcurrentKafkaListenerContainerFactory<String, SaveDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactorySave());

    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, RequestDTO> kafkaListenerContainerFactoryRequest() {

    ConcurrentKafkaListenerContainerFactory<String, RequestDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactoryRequest());

    return factory;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, DeleteDTO> kafkaListenerContainerFactoryDelete() {

    ConcurrentKafkaListenerContainerFactory<String, DeleteDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactoryDelete());

    return factory;
  }

}
