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

  void termTag(RequestDTO requestDTO);

  void matchText(RequestDTO message);

  void matchPhraseText(RequestDTO requestDTO);

  void multiMatchTagText(RequestDTO requestDTO);

  List getStateCompletion(String requestDTO);

  ResultDTO findAll();

}
