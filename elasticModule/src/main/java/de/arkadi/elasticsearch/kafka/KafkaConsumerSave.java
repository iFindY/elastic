package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaConsumerSave {

  private MessageService messageService;

  public KafkaConsumerSave(MessageService messageService) {

    this.messageService = messageService;

  }

  @KafkaListener(topics = "${kafka.in.save.topic}", containerFactory = "kafkaListenerContainerFactorySave")
  public void save(SaveDTO saveDTO) {

    messageService.save(saveDTO);
  }
}
