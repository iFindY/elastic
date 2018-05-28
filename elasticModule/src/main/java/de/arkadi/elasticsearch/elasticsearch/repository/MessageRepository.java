package de.arkadi.elasticsearch.elasticsearch.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import de.arkadi.elasticsearch.model.RequestDTO;
import de.arkadi.elasticsearch.model.ResultDTO;
import de.arkadi.elasticsearch.model.SaveDTO;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
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
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.term.TermSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.*;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@PropertySource("classpath:/application.properties")
public class MessageRepository {

  private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);
  String queryUrl = "/twitterindex/_doc/_search";
  String saveURL = "/twitterindex/_doc/";
  private Map<String, String> params = Collections.emptyMap();
  private RestHighLevelClient client;
  private RestClient restClient;
  private String save;
  private String settings;
  private String mapping;
  private String completion;
  private String index;
  private final String docType = "_doc";
  private final String textField = "message";
  private final String idField = "id";
  private final String tags = "tags";
  private final String users = "users";
  private final String time = "timeStamp";
  private final String input = "input";
  private final String locationSuggest = "userLocationSuggest";
  private boolean dev = false;

  public MessageRepository(RestHighLevelClient client,
                           RestClient restClient,
                           String index,
                           String settings,
                           String mapping,
                           String completion,
                           String save) {

    this.client = client;
    this.index = index;
    this.settings = settings;
    this.mapping = mapping;
    this.restClient = restClient;
    this.completion = completion;
    this.save = save;

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

  public Response save(SaveDTO message) throws IOException {

    HttpEntity save = new NStringEntity(message.createSaveRequest(this.save),
                                        ContentType.APPLICATION_JSON);
    return restClient.performRequest("POST", saveURL, params, save);
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

    searchSourceBuilder.query(matchQueryBuilder).from(0).size(20);
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

  //TODO
  public List<String> getCompletion(String completion) throws IOException {

    SearchRequest searchRequest =
      new SearchRequest(index)
        .source(new SearchSourceBuilder()
                  .suggest(new SuggestBuilder()
                             .addSuggestion("suggest_location",
                                            SuggestBuilders
                                              .termSuggestion("userLocation.input")
                                              .text(completion))));

    SearchResponse searchResponse = client.search(searchRequest);

    Suggest suggest = searchResponse.getSuggest();
    TermSuggestion termSuggestion = suggest.getSuggestion("suggest_location");

    return termSuggestion.getEntries()
      .stream()
      .map(Suggest.Suggestion.Entry::getOptions)
      .flatMap(Collection::stream)
      .map(x -> x.getText().string())
      .collect(Collectors.toList());
  }

  public List getStateCompletion(String request) throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    HttpEntity suggest = new NStringEntity(completion.replace("?", request),
                                           ContentType.APPLICATION_JSON);
    Response result = restClient.performRequest("GET", queryUrl, params, suggest);

    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {

    };

    Map<String, Object> map = mapper.readValue(EntityUtils.toString(result.getEntity()), typeRef);
    List<HashMap<String, Object>> completion =
      (List<HashMap<String, Object>>)
        (((List<HashMap<String, Object>>)
          ((HashMap<String, Object>) map
            .get("suggest")).get("location_suggest"))
          .get(0)).get("options");

    return completion.stream()
      .map(x -> (String) ((HashMap<String, Object>) x.get("_source")).get("userLocationSuggest"))
      .distinct()
      .collect(Collectors.toList());
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
