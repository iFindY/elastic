package de.arkadi.elasticsearch.elasticsearch.repository;

import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;
import de.arkadi.elasticsearch.model.Result;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//@Repository
public class MessageRepository {

  private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);
  private RestHighLevelClient client;
  private String index;
  private final String docType = "doc";
  private final String textField = "message";
  private final String idField = "id";

  public MessageRepository(RestHighLevelClient client, String index) {

    this.client = client;
    this.index = index;
  }

  public void save(Message message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest(index, docType, message.getId())
        .source(idField, message.getId(),
                textField, message.getMessage());

    log.info("save status " + client.index(indexRequest).status().toString());
  }

  public void save(String id, String message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest(index, docType, id)
        .source(idField, id,
                textField, message);

    log.info("save status " + client.index(indexRequest).status().toString());
  }

  public void saveAll(List<Message> messages) throws IOException {

    BulkRequest bulkRequest = new BulkRequest();
    messages.stream().map(this::assembleIndexRequest).forEach(bulkRequest::add);

    log.info("saveAll status " + client.bulk(bulkRequest).status().toString());
  }

  public void deleteById(String id) throws IOException {

    DeleteRequest request = new DeleteRequest(index, docType, id);

    log.info("deleteById status :" + client.delete(request).status().toString());
  }

  public Result findMatch(Request message) throws IOException {

    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(textField, message.getRequest())
      .fuzziness(Fuzziness.AUTO)
      .prefixLength(3)
      .maxExpansions(5);

    searchSourceBuilder.query(matchQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    SearchResponse response = client.search(searchRequest);
    log.info("findMatch status :" + response.status().toString());

    return new Result(Arrays.stream((response).getHits().getHits())
                        .map(hit -> hit.getSourceAsMap().get(idField).toString())
                        .collect(Collectors.toList()));
  }

  public List<Message> findAll() throws IOException {

    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);
    SearchResponse response = client.search(searchRequest);
    log.info("findAll status :" + response.status().toString());

    return Arrays.stream(response.getHits().getHits())
      .map(hit -> new Message(hit.field("id").toString(), hit.field(docType).toString()))
      .collect(Collectors.toList());
  }

  private IndexRequest assembleIndexRequest(@NotNull Message m) {

    return new IndexRequest(index, docType, m.getId())
      .source("id", m.getId(),
              docType, m.getMessage());
  }

  public void createIndex(String index) throws IOException {

    CreateIndexRequest request = new CreateIndexRequest(index);
    request.settings(Settings.builder()
                       .put("index.number_of_shards", 3)
                       .put("index.number_of_replicas", 2)
    );

    request.mapping("mappings",
                    "  {\n" +
                    "    \"doc\": {\n" +
                    "      \"properties\": {\n" +
                    "        \"id\": {\n" +
                    "          \"docType\": \"text\"\n" +
                    "        }\n" +
                    "        \"message\": {\n" +
                    "          \"docType\": \"text\"\n" +
                    "           \"analyzer\": \"english\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }",
                    XContentType.JSON);

    CreateIndexResponse createIndexResponse = client.indices().create(request);
    log.info("Created index : "
             + createIndexResponse.index()
             + " and all of the nodes have acknowledged the request : "
             + createIndexResponse.isAcknowledged()
             + ". The requisite number of shard copies were started for each shard in the index: "
             + createIndexResponse.isShardsAcknowledged()
    );
  }

  public void deleteIndex(String index) throws IOException {

    DeleteIndexRequest request = new DeleteIndexRequest(index);
    DeleteIndexResponse deleteIndexResponse = client.indices().delete(request);
    log.info("Index is deleted properly :" + deleteIndexResponse.isAcknowledged());
  }
}
