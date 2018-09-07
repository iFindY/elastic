package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;

@PropertySource("classpath:/application.properties")
public class KafkaConsumerCompletion {

  private static final Logger log = LoggerFactory.getLogger(KafkaConsumerCompletion.class);
  private MessageService messageService;

  public KafkaConsumerCompletion(MessageService messageService) {

    this.messageService = messageService;
  }

  @KafkaListener(topics = "${kafka.in.completion.topic}", containerFactory = "kafkaListenerContainerFactoryCompletion")
  public void search(RequestDTO dto) {

    messageService.getStateCompletionKafka(dto);
  }

}
