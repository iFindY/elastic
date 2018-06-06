package de.arkadi.elasticsearch.restController;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/arkadi/search")
public class WebRestController {

  private MessageService messageService;

  public WebRestController(MessageService messageService) {

    this.messageService = messageService;
  }

  @GetMapping(value = "/post")
  public void store(@RequestParam("id") String id,
                    @RequestParam("data") String data,
                    @RequestParam("tags") List tags) {

    messageService.save(id, data, tags);

  }

  @GetMapping(value = "/postMessage")
  public void storeMessage(@RequestParam("data") SaveDTO data) {

    messageService.save(data);

  }

  @GetMapping(value = "/getAll")
  public ResultDTO getAllMessage() {

    return messageService.findAll();

  }

/*  @GetMapping(value = "/getMatch")
  public ResultDTO getMatchMessage( RequestDTO message) {

    return messageService.matchText(message);
  }*/

  @GetMapping(value = "/stateCompletion")
  public List getCompleation(@RequestParam("request") String request) {

    return messageService.getStateCompletion(request);

  }

}
