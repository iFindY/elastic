package de.arkadi.elasticsearch.spring;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
@PropertySource("classpath:/application.properties")
public class ElasticsearchConfig {

  private static final Logger log = LoggerFactory.getLogger(ElasticsearchConfig.class);

  @Value("${elasticsearch.host}")
  private String EsHost;

  @Value("${elasticsearch.port}")
  private int EsPort;

  @Value("${elasticsearch.index}")
  private String inIndex;

  @Value("${elasticsearch.settings.path}")
  String settings;

  @Value("${elasticsearch.mapping.path}")
  String mappings;

  @Bean
  public RestHighLevelClient client() {

    return new RestHighLevelClient(RestClient.builder(new HttpHost(EsHost, EsPort, "http")));
  }

  @Bean
  public MessageRepository messageRepository(ApplicationContext context) {

    String settings = null;
    String mappings = null;
    try {
      settings = new BufferedReader(
        new InputStreamReader(
          context.getResource("classpath:" + this.settings).getInputStream())).lines()
        .collect(Collectors.joining("\n"));
      mappings = new BufferedReader(
        new InputStreamReader(
          context.getResource("classpath:" + this.mappings).getInputStream())).lines()
        .collect(Collectors.joining("\n"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    RestClient restClient = RestClient.builder(new HttpHost(EsHost, EsPort, "http"))
      .build();

    return new MessageRepository(client(), restClient, inIndex, settings, mappings);
  }

}
