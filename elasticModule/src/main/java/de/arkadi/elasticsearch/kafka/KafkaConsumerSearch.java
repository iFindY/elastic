package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;
@PropertySource("classpath:/application.properties")
public class KafkaConsumerSearch {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerResult.class);
  private MessageService messageService;

  public KafkaConsumerSearch(MessageService messageService) {

    this.messageService = messageService;
  }

  @KafkaListener(topics = "${kafka.in.search.topic}", containerFactory = "kafkaListenerContainerFactoryRequest")
  public void search(RequestDTO dto) {

    log.info("received search request = '{}'", dto.getText());
    messageService.matchText(dto);
  }
}
