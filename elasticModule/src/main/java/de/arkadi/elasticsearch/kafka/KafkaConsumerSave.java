package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaConsumerSave {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerResult.class);
  private MessageService messageService;

  public KafkaConsumerSave(MessageService messageService) {

    this.messageService = messageService;

  }

  @KafkaListener(topics = "${kafka.in.save.topic}", containerFactory = "kafkaListenerContainerFactory")
  public void save(SaveDTO dto) {

    log.info("Elasticsearch received content = '{}'" + dto.getText());
    messageService.save(dto);
  }
}
