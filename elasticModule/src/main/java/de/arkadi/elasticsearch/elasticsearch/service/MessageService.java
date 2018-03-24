package de.arkadi.elasticsearch.elasticsearch.service;

import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;

import java.util.List;

public interface MessageService {

  void save(Message message);

  void save(String id, String message);

  void saveAll(List<Message> message);

  void deleteById(String id);

  void findMatch(Request message);

  List<Message> findAll();

}
