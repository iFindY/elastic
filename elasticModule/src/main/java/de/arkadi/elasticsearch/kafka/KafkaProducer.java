package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducer {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
  private final KafkaTemplate<String, Result> kafkaTemplate;

  private String kafkaTopic;

  public KafkaProducer(KafkaTemplate<String, Result> kafkaTemplate, String kafkaTopic) {

    this.kafkaTemplate = kafkaTemplate;
    this.kafkaTopic = kafkaTopic;
  }

  public void send(Result result) {

    if (result != null) {
      log.info("sending data='{}'", result);
      kafkaTemplate.send(kafkaTopic, result);
    }
  }
}
