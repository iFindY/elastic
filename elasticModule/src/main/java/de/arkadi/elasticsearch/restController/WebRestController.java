package de.arkadi.elasticsearch.restController;

import de.arkadi.elasticsearch.elasticsearch.service.MessageService;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/arkadi/search")
public class WebRestController {

  private MessageService messageService;

  public WebRestController(MessageService messageService) {

    this.messageService = messageService;
  }

  @GetMapping(value = "/post")
  public void store(@RequestParam("id") String id, @RequestParam("data") String data) {

    messageService.save(id, data);

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

    return messageService.findMatch(message);
  }*/

}
