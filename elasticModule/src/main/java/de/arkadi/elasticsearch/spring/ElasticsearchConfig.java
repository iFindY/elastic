package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchConfig.class);

  @Value("${elasticsearch.host}")
  private String EsHost;

  @Value("${elasticsearch.port}")
  private int EsPort;

  @Value("${elasticsearch.clustername}")
  private String EsClusterName;

  @Value("${elasticsearch.index}")
  private String inIndex;

  @PostConstruct
  public void init() throws IOException {

    try {
      messageRepository().createIndex(inIndex);
    }
    catch (Exception e) {
      messageRepository().deleteIndex(inIndex);
      messageRepository().createIndex(inIndex);
      log.warn(inIndex + " was recreated all data lost");
    }

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
