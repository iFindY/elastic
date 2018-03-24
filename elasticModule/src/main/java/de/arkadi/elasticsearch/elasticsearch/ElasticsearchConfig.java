package de.arkadi.elasticsearch.elasticsearch;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

@Configuration
@PropertySource("classpath:/application.properties")
@EnableAutoConfiguration
@ComponentScan(basePackages = { "de.arkadi.elasticsearch.elasticsearch.repository" })
public class ElasticsearchConfig {

  @Value("${elasticsearch.host}")
  private String EsHost;

  @Value("${elasticsearch.port}")
  private int EsPort;

  @Value("${elasticsearch.clustername}")
  private String EsClusterName;

  @PostConstruct
  public void init() {

    //elasticsearchTemplate.deleteIndex(Message.class);
    //elasticsearchTemplate.createIndex(Message.class);
    //elasticsearchTemplate.putMapping(Message.class);
    //elasticsearchTemplate.refresh(Message.class);
  }

  @Bean
  public RestHighLevelClient client() {

    return new RestHighLevelClient(RestClient.builder(new HttpHost(EsHost, EsPort, "http")));
  }

  @Bean
  public MessageRepository messageRepository() {

    return new MessageRepository(client());
  }

}
