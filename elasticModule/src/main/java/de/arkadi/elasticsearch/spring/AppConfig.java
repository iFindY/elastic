package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.elasticsearch.ElasticsearchConfig;
import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.elasticsearch.service.MessageServiceImpl;
import de.arkadi.elasticsearch.kafka.KafkaConsumer;
import de.arkadi.elasticsearch.kafka.KafkaProducer;
import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Result;
import de.arkadi.elasticsearch.twitter.MessageStorageTesting;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAutoConfiguration
@PropertySource("classpath:/application.properties")
@Import(ElasticsearchConfig.class)
public class AppConfig {

  @Autowired
  Environment environment;
  @Autowired
  MessageRepository messageRepository;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public Map<String, Object> producerConfigs() {

    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
              environment.getProperty("kafka.bootstrap-servers"));
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public Map<String, Object> consumerConfig() {

    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
              environment.getProperty("kafka.bootstrap-servers"));
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("kafka.consumer.group-id"));
    return props;
  }

  @Bean
  public ConsumerFactory<String, Message> consumerFactory() {

    return new DefaultKafkaConsumerFactory<>(consumerConfig(),
                                             new StringDeserializer(),
                                             new JsonDeserializer<>(Message.class));
  }

  @Bean
  public ProducerFactory<String, Result> producerFactory() {

    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, Result> kafkaTemplate() {

    return new KafkaTemplate<>(producerFactory());
  }

/*    @Bean
    public TwitterKafkaService TwitterKafkaStreamService() {

        ConfigurationBuilder configuration = new ConfigurationBuilder();
        configuration.setDebugEnabled( true )
                .setOAuthConsumerKey( environment.getProperty( "twitter.ConsumerKey" ) )
                .setOAuthConsumerSecret( environment.getProperty( "twitter.ConsumerSecret" ) )
                .setOAuthAccessToken( environment.getProperty( "twitter.AccessToken" ) )
                .setOAuthAccessTokenSecret( environment.getProperty( "twitter.AccessTokenSecret" ) );

        return new TwitterKafkaService( configuration, kafkaProducer() );
    }*/

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Message> kafkaListenerContainerFactory() {

    ConcurrentKafkaListenerContainerFactory<String, Message> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());

    return factory;
  }

  @Bean
  public KafkaProducer kafkaProducer() {

    return new KafkaProducer(kafkaTemplate(), environment.getProperty("kafka.out.topic"));
  }

  @Bean
  public MessageStorageTesting messageStorage() {

    return new MessageStorageTesting();
  }

  @Bean
  public KafkaConsumer kafkaConsumer() {

    return new KafkaConsumer(messageService());
  }

  @Bean
  public MessageService messageService() {

    return new MessageServiceImpl(messageRepository, kafkaProducer());
  }
}
