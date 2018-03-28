package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Map;

public class KafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
  private MessageService messageService;

  public KafkaConsumer(MessageService messageService) {

    this.messageService = messageService;

  }

  @KafkaListener(topics = "${kafka.in.topic}", containerFactory = "kafkaListenerContainerFactory")
  public void store(Map<String, String> json) {

    if (json.get("id") != null) {
      Message m = new Message(json.get("id"), json.get("message"));
      log.info("Elasticsearch received content = '{}'" + m);
      messageService.save(m);
    }
    //TODO  redirect to cassandraTopic
    else if (json.get("request") != null) {
      Request text = new Request(json.get("request"));
      log.info("Elasticsearch received query = '{}'" + text);
      messageService.findMatch(text);
    }
    else {
      log.error("Incompatible Document : " + json);
    }
  }

}
