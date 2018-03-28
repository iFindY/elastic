package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.elasticsearch.service.MessageServiceImpl;
import de.arkadi.elasticsearch.kafka.KafkaConsumer;
import de.arkadi.elasticsearch.kafka.KafkaProducer;
import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Result;
import de.arkadi.elasticsearch.twitter.TwitterKafkaServiceTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import twitter4j.conf.ConfigurationBuilder;

@Configuration
@EnableAutoConfiguration
@PropertySource("classpath:/application.properties")
@Import({ KafkaConfig.class, ElasticsearchConfig.class })
public class AppConfig {

  @Value("${twitter.ConsumerKey}")
  private String key;
  @Value("${twitter.ConsumerSecret}")
  private String keySecret;
  @Value("${twitter.AccessToken}")
  private String token;
  @Value("${twitter.AccessTokenSecret}")
  private String tokenSecret;
  @Value("${kafka.out.topic}")
  private String kafkaOutTopic;
  @Value("${kafka.in.topic}")
  private String kafkaInTopic;

  @Autowired KafkaTemplate<String, Result> kafkaTemplate;
  @Autowired KafkaTemplate<String, Message> kafkaTemplateTest;
  @Autowired MessageRepository messageRepository;

  /**
   * Getting Twitter data
   */
  @Bean
  public TwitterKafkaServiceTesting TwitterKafkaStreamService() {

    ConfigurationBuilder configuration = new ConfigurationBuilder();
    configuration.setDebugEnabled(true)
      .setOAuthConsumerKey(key)
      .setOAuthConsumerSecret(keySecret)
      .setOAuthAccessToken(token)
      .setOAuthAccessTokenSecret(tokenSecret);

    return new TwitterKafkaServiceTesting(configuration, kafkaProducerTest());
  }

  /**
   * create a producer which will push data to topic. It uses a spring template and a kafka address.
   */
  @Bean
  public KafkaProducer kafkaProducer() {

    return new KafkaProducer(kafkaTemplate, kafkaOutTopic);
  }

  /**
   * consume messages to store or  search requests to answer
   */
  @Bean
  public KafkaConsumer kafkaConsumer() {

    return new KafkaConsumer(messageService());
  }

  /**
   * it create a Service which communicants with Elasticsearch.
   * read/write Service.
   * it takes an elasticsearch and an kafka connector.
   */
  @Bean
  public MessageService messageService() {

    return new MessageServiceImpl(messageRepository, kafkaProducer());
  }

  //-------Test area----------//

  /**
   * read out twitter and  push it  to kafka
   */
  @Bean KafkaProducer kafkaProducerTest() {

    return new KafkaProducer(kafkaTemplate, kafkaInTopic);
  }
}
