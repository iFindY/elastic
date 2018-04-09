package de.arkadi.elasticsearch.kafka;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SearchDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaConsumerSearch {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducerResult.class);
  private MessageService messageService;
  private KafkaProducerResult kafkaProducer;

  public KafkaConsumerSearch(MessageService messageService, KafkaProducerResult kafkaProducer) {

    this.messageService = messageService;
    this.kafkaProducer = kafkaProducer;

  }

  @KafkaListener(topics = "${kafka.in.search.topic}", containerFactory = "kafkaListenerContainerFactory")
  public void search(SearchDTO dto) {

    log.info("Elasticsearch received search query = '{}'" + dto.getRequest());
    messageService.findMatch(dto);
  }
}
