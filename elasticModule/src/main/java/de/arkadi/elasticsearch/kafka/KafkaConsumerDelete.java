package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.DeleteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaConsumerDelete {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerResult.class);
  private MessageService messageService;

  public KafkaConsumerDelete(MessageService messageService) {

    this.messageService = messageService;

  }

  @KafkaListener(topics = "${kafka.in.delete.topic}", containerFactory = "kafkaListenerContainerFactoryDelete")
  public void delete(DeleteDTO dto) {

    String id = dto.getId();
    log.info("received delete request = '{}'" + id);
    messageService.deleteById(id);
  }
}
