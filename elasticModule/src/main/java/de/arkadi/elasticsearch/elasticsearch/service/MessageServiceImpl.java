package de.arkadi.elasticsearch.elasticsearch.service;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import de.arkadi.elasticsearch.kafka.KafkaProducerResult;
import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;

import java.io.IOException;
import java.util.List;

public class MessageServiceImpl implements MessageService {

  private MessageRepository messageRepository;
  private KafkaProducerResult kafkaProducer;

  public MessageServiceImpl(MessageRepository messageRepository,
                            KafkaProducerResult kafkaProducer) {

    this.messageRepository = messageRepository;
    this.kafkaProducer = kafkaProducer;
  }

  @Override
  public void save(SaveDTO message) {

    try {
      messageRepository.save(message);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void save(String id, String message, List tags) {

    try {
      messageRepository.save(id, message, tags);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void saveAll(List<SaveDTO> messages) {

    try {
      messageRepository.saveAll(messages);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deleteById(String id) {

    try {
      messageRepository.deleteById(id);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void findMatch(RequestDTO message) {

    try {
      ResultDTO result = messageRepository.matchText(message);
      kafkaProducer.send(result);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public ResultDTO findAll() {

    try {
      return messageRepository.matchAll();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
