package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.model.ResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducerResult {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerResult.class);
  private final KafkaTemplate<String, ResultDTO> kafkaTemplate;

  private String kafkaTopic;

  public KafkaProducerResult(KafkaTemplate<String, ResultDTO> kafkaTemplate, String kafkaTopic) {

    this.kafkaTemplate = kafkaTemplate;
    this.kafkaTopic = kafkaTopic;
  }

  public void send(ResultDTO result) {

    log.info("sending data='{}'", result);
    kafkaTemplate.send(kafkaTopic, result);
  }
}
