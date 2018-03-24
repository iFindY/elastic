package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
  private MessageService messageService;

  public KafkaConsumer(MessageService messageService) {

    this.messageService = messageService;

  }

  @KafkaListener(topics = "${kafka.in.topic}", containerFactory = "kafkaListenerContainerFactory")
  public <T> void storeMessage(T content) {

    if (content instanceof Message) {
      log.info("Elasticsearch received content = '{}'" + content);
      messageService.save((Message) content);
    }
    else if (content instanceof Request) {
      log.info("Elasticsearch received query = '{}'" + content);
      messageService.findMatch((Request) content);
    }
    else {
      System.err.println("Incompatible Document : " + content);
    }

  }

}
