package de.arkadi.elasticsearch.spring;

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
import java.io.IOException;

@Configuration
@PropertySource("classpath:/application.properties")
public class ElasticsearchConfig {

  @Value("${elasticsearch.host}")
  private String EsHost;

  @Value("${elasticsearch.port}")
  private int EsPort;

  @Value("${elasticsearch.clustername}")
  private String EsClusterName;

  @Value("${elasticsearch.in}")
  private String inIndex;

  @Value("${elasticsearch.out}")
  private String outIndex;

  @PostConstruct
  public void init() throws IOException {

    messageRepository().deleteIndex(inIndex);
    messageRepository().createIndex(inIndex);
  }

  @Bean
  public RestHighLevelClient client() {

    return new RestHighLevelClient(RestClient.builder(new HttpHost(EsHost, EsPort, "http")));
  }

  @Bean
  public MessageRepository messageRepository() {

    return new MessageRepository(client(), inIndex);
  }

}
