package de.arkadi.elasticsearch.elasticsearch.service;

import de.arkadi.elasticsearch.elasticsearch.repository.MessageRepository;
import de.arkadi.elasticsearch.kafka.KafkaProducer;
import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;

import java.io.IOException;
import java.util.List;

public class MessageServiceImpl implements MessageService {

  private MessageRepository messageRepository;
  private KafkaProducer kafkaProducer;

  public MessageServiceImpl(MessageRepository messageRepository, KafkaProducer kafkaProducer) {

    this.messageRepository = messageRepository;
    this.kafkaProducer = kafkaProducer;
  }

  @Override
  public void save(Message message) {

    try {
      messageRepository.save(message);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override public void save(String id, String message) {

    try {
      messageRepository.save(id, message);
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void saveAll(List<Message> messages) {

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
  public void findMatch(Request message) {

    try {
      kafkaProducer.send(messageRepository.findMatch(message));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<Message> findAll() {

    try {
      return messageRepository.findAll();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
