package de.arkadi.elasticsearch.elasticsearch.repository;

import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
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
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@PropertySource("classpath:/application.properties")
public class MessageRepository {

  private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);
  private RestHighLevelClient client;
  private String settings;
  private String mapping;
  private String index;
  private final String docType = "_doc";
  private final String textField = "message";
  private final String idField = "id";
  private final String tags = "tags";
  private RestClient restClient;
  private boolean dev = false;

  public MessageRepository(RestHighLevelClient client,
                           RestClient restClient,
                           String index,
                           String settings,
                           String mapping) {

    this.client = client;
    this.index = index;
    this.settings = settings;
    this.mapping = mapping;
    this.restClient = restClient;

  }

  @PostConstruct
  public void init() throws IOException {

    try {
      createIndexRest(index);
    }
    catch (Exception e) {
      log.warn("Index '{}' exists", index);
      if (dev) {
        deleteIndex(index);
        createIndexRest(index);
        log.warn("Index '{}' was recreated all data lost", index);
      }
    }

  }

  public RestStatus save(SaveDTO message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest(index, docType, message.getId())
        .source(idField, message.getId(),
                textField, message.getText(),
                this.tags, message.getTags());

    return client.index(indexRequest).status();
  }

  public RestStatus save(String id, String message, List tags) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest(index, docType, id)
        .source(idField, id,
                textField, message,
                this.tags, tags);

    return client.index(indexRequest).status();
  }

  public RestStatus saveAll(List<SaveDTO> messages) throws IOException {

    BulkRequest bulkRequest = new BulkRequest();
    messages.stream().map(this::assembleIndexRequest).forEach(bulkRequest::add);

    return client.bulk(bulkRequest).status();
  }

  public RestStatus deleteById(String id) throws IOException {

    DeleteRequest request = new DeleteRequest(index, docType, id);
    DeleteResponse deleteResponse = client.delete(request);
    return deleteResponse.status();
  }

  public ResultDTO termTag(RequestDTO requestDTO) throws IOException {

    SearchRequest searchRequest =
      new SearchRequest(index)
        .source(new SearchSourceBuilder()
                  .query(new TermsQueryBuilder(tags, requestDTO.getTags())));

    SearchResponse response = client.search(searchRequest);
    return new ResultDTO(getResult(response),
                         requestDTO.getRequest_id(),
                         requestDTO.getAnswer_partition());
  }

  public ResultDTO matchText(RequestDTO message) throws IOException {

    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(textField, message.getText())
      .fuzziness(Fuzziness.AUTO)
      .prefixLength(3)
      .maxExpansions(5);

    searchSourceBuilder.query(matchQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    SearchResponse response = client.search(searchRequest);

    List<String> resultList = Arrays.stream((response).getHits().getHits())
      .map(hit -> hit.getSourceAsMap().get(idField).toString())
      .collect(Collectors.toList());

    return new ResultDTO(resultList,
                         message.getRequest_id(),
                         message.getAnswer_partition());
  }

  public ResultDTO matchPhraseText(RequestDTO requestDTO) throws IOException {

    SearchRequest searchRequest =
      new SearchRequest(index)
        .source(new SearchSourceBuilder()
                  .query(new MatchPhraseQueryBuilder(textField, requestDTO.getText()).slop(1)));

    SearchResponse response = client.search(searchRequest);
    return new ResultDTO(this.getResult(response),
                         requestDTO.getRequest_id(),
                         requestDTO.getAnswer_partition());

  }

  public ResultDTO multiMatchTagText(RequestDTO requestDTO) throws IOException {

    SearchRequest searchRequest =
      new SearchRequest(index)
        .source(new SearchSourceBuilder()
                  .query(new MultiMatchQueryBuilder(
                    requestDTO.getText(), textField, this.tags)));

    SearchResponse response = client.search(searchRequest);
    return new ResultDTO(this.getResult(response),
                         requestDTO.getRequest_id(),
                         requestDTO.getAnswer_partition());
  }

  public ResultDTO matchAll() throws IOException {

    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);
    SearchResponse response = client.search(searchRequest);

    List<String> result = Arrays.stream(response.getHits().getHits())
      .map(hit -> hit.field(idField).toString())
      .collect(Collectors.toList());

    return new ResultDTO(result);
  }

  @Deprecated
  public void createIndex(String index) throws IOException {

    CreateIndexRequest request = new CreateIndexRequest(index);

    XContentBuilder settingsBuilder = jsonBuilder().startObject()
      .startObject("settings")
      .field("index.number_of_shards", 5)
      .field("index.number_of_replicas", 0)
      .field("refresh_interval", "5s")
      .startObject("analysis")
      .startObject("analyzer")
      .startObject("my_analyzer")
      .field("type", "custom")
      .field("tokenizer", "standard")
      .startArray("filter")
      .value("standard")
      .value("lowercase")
      .value("word_delimiter")
      .value("my_stemmer")
      .value("english_possessive_stemmer")
      .value("my_stop")
      .endArray()
      .endObject()
      .endObject()
      .startObject("filter")
      .startObject("my_stop")
      .field("type", "stop")
      .field("stopwords", "_english_")
      .endObject()
      .startObject("my_stemmer")
      .field("type", "stemmer")
      .field("language", "english")
      .endObject()
      .startObject("english_possessive_stemmer")
      .field("type", "stemmer")
      .field("name", "possessive_stemmer")
      .endObject()
      .endObject()
      .endObject()
      .endObject()
      .endObject();

    XContentBuilder mappingsBuilder = jsonBuilder().startObject()
      .field("dynamic", false)
      .startObject("properties")
      .startObject(idField)
      .field("type", "keyword")
      .endObject()
      .startObject(textField)
      .field("type", "text")
      .field("analyzer", "english")
      .endObject()
      .startObject(tags)
      .field("type", "keyword")
      .field("analyzer", "keyword")
      .endObject()
      .endObject()
      .endObject();
    request.settings(settingsBuilder).mapping(docType, mappingsBuilder);

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

  public void createIndexRest(String index) {

    Map<String, String> params = Collections.emptyMap();
    String url = "http://134.100.11.157:9200/";
    HttpEntity settings = new NStringEntity(this.settings, ContentType.APPLICATION_JSON);
    HttpEntity mappings = new NStringEntity(this.mapping, ContentType.APPLICATION_JSON);

    try {
      restClient.performRequest("PUT", url + index, params, settings);
      restClient.performRequest("PUT", url + index + "/_mappings/" + docType, params, mappings);

    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private IndexRequest assembleIndexRequest(@NotNull SaveDTO m) {

    return new IndexRequest(index, docType, m.getId())
      .source("id", m.getId(),
              docType, m.getText());
  }

  private List<String> getResult(SearchResponse response) {

    return Arrays.stream((response).getHits().getHits())
      .map(hit -> hit.getSourceAsMap().get(idField).toString())
      .collect(Collectors.toList());

  }
}
