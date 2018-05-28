package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.HashMap;
import java.util.Map;

public class KafkaConsumerSave {

  private MessageService messageService;

  public KafkaConsumerSave(MessageService messageService) {

    this.messageService = messageService;

  }

  @KafkaListener(topics = "${kafka.in.save.topic}", containerFactory = "kafkaListenerContainerFactoryTest")
  public void save(HashMap saveRequest) {

    messageService.save(new SaveDTO(saveRequest));
  }
}
