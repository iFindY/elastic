package de.arkadi.elasticsearch.elasticsearch.repository;

import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class MessageRepository {

  private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);
  private RestHighLevelClient client;
  private String settings;
  private String mapping;
  private String index;
  private final String docType = "doc";
  private final String textField = "message";
  private final String idField = "id";

  public MessageRepository(RestHighLevelClient client,
                           String index,
                           String settings,
                           String mapping) {

    this.client = client;
    this.index = index;
    this.settings = settings;
    this.mapping = mapping;
  }

  @PostConstruct
  public void init() throws IOException {

    try {
      createIndex(index);
    }
    catch (Exception e) {
      deleteIndex(index);
      createIndex(index);
      log.warn("Index '{}' was recreated all data lost", index);
    }

  }

  public void save(SaveDTO message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest(index, docType, message.getId())
        .source(idField, message.getId(),
                textField, message.getText());

    log.info("save status " + client.index(indexRequest).status().toString());
  }

  public void save(String id, String message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest(index, docType, id)
        .source(idField, id,
                textField, message);

    log.info("Text {} saved '{}' under id {} ",
             message,
             client.index(indexRequest).status().toString(),
             id);
  }

  public void saveAll(List<SaveDTO> messages) throws IOException {

    BulkRequest bulkRequest = new BulkRequest();
    messages.stream().map(this::assembleIndexRequest).forEach(bulkRequest::add);

    log.info("saveAll status " + client.bulk(bulkRequest).status().toString());
  }

  public void deleteById(String id) throws IOException {

    DeleteRequest request = new DeleteRequest(index, docType, id);
    DeleteResponse deleteResponse = client.delete(request);
    log.info("Text with '{}' deleted :'{}'", id, deleteResponse.status().toString());
  }

  public ResultDTO findMatch(RequestDTO message) throws IOException {

    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(textField, message.getText())
      .fuzziness(Fuzziness.AUTO)
      .prefixLength(3)
      .maxExpansions(5);

    searchSourceBuilder.query(matchQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    SearchResponse response = client.search(searchRequest);
    log.info("findMatch status :'{}'", response.status().toString());

    List<String> resultList = Arrays.stream((response).getHits().getHits())
      .map(hit -> hit.getSourceAsMap().get(idField).toString())
      .collect(Collectors.toList());

    return new ResultDTO(resultList,
                         message.getRequest_id(),
                         message.getAnswer_partition());
  }

  public ResultDTO findAll() throws IOException {

    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);
    SearchResponse response = client.search(searchRequest);
    log.info("findAll status :" + response.status().toString());

    List<String> result = Arrays.stream(response.getHits().getHits())
      .map(hit -> hit.field("text").toString())
      .collect(Collectors.toList());

    return new ResultDTO(result);
  }

  private IndexRequest assembleIndexRequest(@NotNull SaveDTO m) {

    return new IndexRequest(index, docType, m.getId())
      .source("id", m.getId(),
              docType, m.getText());
  }

  public void createIndex(String index) throws IOException {

    CreateIndexRequest request = new CreateIndexRequest(index);
    //request.settings(settings, XContentType.JSON);
    //request.mapping(docType, mapping, XContentType.JSON);

    request.settings(Settings.builder()
                       .put("index.number_of_shards", 5)
                       .put("index.number_of_replicas", 0)
                       .put("refresh_interval", "5s"));
    //request.mapping("{\"mappings\":{\"doc\":{\"properties\":{\"id\":{\"type\":\"text\"},\"message\":{\"type\":\"text\",\"analyzer\":\"english\"}}}}}", XContentType.JSON);
    XContentBuilder builder = jsonBuilder().startObject()
      .startObject("properties")
      .startObject(idField)
      .field("type", "text")
      .endObject()
      .startObject(textField)
      .field("type", "text")
      .field("analyzer", "english")
      .field("store", "false")
      .endObject()
      .endObject()
      .endObject();
    request.mapping(docType, builder);

    CreateIndexResponse createIndexResponse = client.indices().create(request);
    log.info(
      "\nCreated '{}' index "
      + "\nAll of the nodes have acknowledged the request: '{}'"
      + "\nThe requisite number of shard copies were started for each shard in the index: '{}'",
      createIndexResponse.index(),
      createIndexResponse.isAcknowledged(),
      createIndexResponse.isShardsAcknowledged()
    );
  }

  public void deleteIndex(String index) throws IOException {

    DeleteIndexRequest request = new DeleteIndexRequest(index);
    DeleteIndexResponse deleteIndexResponse = client.indices().delete(request);
    log.info("Index '{}' is deleted properly :'{}'", index, deleteIndexResponse.isAcknowledged());
  }

}
