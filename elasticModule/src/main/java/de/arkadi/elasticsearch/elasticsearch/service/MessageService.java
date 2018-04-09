package de.arkadi.elasticsearch.elasticsearch.service;

import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import de.arkadi.elasticsearch.model.SearchDTO;

import java.util.List;

public interface MessageService {

  void save(SaveDTO message);

  void save(String id, String message);

  void saveAll(List<SaveDTO> message);

  void deleteById(String id);

  void findMatch(SearchDTO message);

  List<SaveDTO> findAll();

}
