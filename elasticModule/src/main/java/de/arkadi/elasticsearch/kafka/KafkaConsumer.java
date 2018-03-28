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

    System.out.println("breakpoint");
    if (json.get("id") != null) {
      Message m = new Message(json.get("id"), json.get("text"));
      log.info("Elasticsearch received content = '{}'" + m);
      messageService.save(m);
    }
    else if (json.get("requestList") != null) {
      Request r = new Request(json.get("requestList"));
      log.info("Elasticsearch received query = '{}'" + r);
      messageService.findMatch(r);
    }
    else {
      log.error("Incompatible Document : " + json);
    }
  }

}
