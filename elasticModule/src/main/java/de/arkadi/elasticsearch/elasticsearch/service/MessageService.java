package de.arkadi.elasticsearch.elasticsearch.service;

import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;

import java.util.List;

public interface MessageService {

  void save(SaveDTO message);

  void save(String id, String message, List tags);

  void saveAll(List<SaveDTO> message);

  void deleteById(String id);

  void findMatch(RequestDTO message);

  ResultDTO findAll();

}
