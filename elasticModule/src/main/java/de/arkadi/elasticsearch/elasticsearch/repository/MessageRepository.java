package de.arkadi.elasticsearch.elasticsearch.repository;

import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;
import de.arkadi.elasticsearch.model.Result;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;

@Repository
public class MessageRepository {

  private RestHighLevelClient client;
  private static final Logger log = LoggerFactory.getLogger(MessageRepository.class);

  public MessageRepository(RestHighLevelClient client) {

    this.client = client;
  }

  public void save(Message message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest("twitterindex", "message", message.getId())
        .source("id", message.getId(),
                "message", message.getMessage());

    client.index(indexRequest);
  }

  public void save(String id, String message) throws IOException {

    IndexRequest indexRequest =
      new IndexRequest("twitterindex", "message", id)
        .source("id", id,
                "message", message);

    client.index(indexRequest);
  }

  public void saveAll(List<Message> messages) throws IOException {

    BulkRequest bulkRequest = new BulkRequest();
    messages.stream().map(this::transform).forEach(bulkRequest::add);
    client.bulk(bulkRequest);
  }

  public void deleteById(String id) throws IOException {

    DeleteRequest request = new DeleteRequest("twitterindex", "message", id);
    client.delete(request);
  }

  public Result findMatch(Request message) throws IOException {

    SearchRequest searchRequest = new SearchRequest("twitterindex");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("message", message.getRequest())
      .fuzziness(Fuzziness.AUTO)
      .prefixLength(3)
      .maxExpansions(5);

    searchSourceBuilder.query(matchQueryBuilder);
    searchRequest.source(searchSourceBuilder);

    return new Result(Arrays.stream((client.search(searchRequest)).getHits().getHits())
                        .map(hit -> hit.getSourceAsMap().get("id").toString())
                        .collect(Collectors.toList()));
  }

  public List<Message> findAll() throws IOException {

    SearchRequest searchRequest = new SearchRequest("twitterindex");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    searchRequest.source(searchSourceBuilder);

    return Arrays.stream(client.search(searchRequest).getHits().getHits())
      .map(hit -> new Message(hit.field("id").toString(), hit.field("message").toString()))
      .collect(Collectors.toList());
  }

  private IndexRequest transform(Message m) {

    return new IndexRequest("twitterindex", "message", m.getId())
      .source("id", m.getId(),
              "message", m.getMessage());
  }

  public void createIndex(String index) throws IOException {

    CreateIndexRequest request = new CreateIndexRequest(index);
    request.settings(Settings.builder()
                       .put("index.number_of_shards", 3)
                       .put("index.number_of_replicas", 2)
    );

    request.mapping("mappings",
                    "  {\n" +
                    "    \"message\": {\n" +
                    "      \"properties\": {\n" +
                    "        \"id\": {\n" +
                    "          \"type\": \"text\"\n" +
                    "        }\n" +
                    "        \"message\": {\n" +
                    "          \"type\": \"text\"\n" +
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

  public void dropIndex(String index) throws IOException {

    DeleteIndexRequest request = new DeleteIndexRequest(index);
    DeleteIndexResponse deleteIndexResponse = client.indices().delete(request);
    log.info("Index is deleted properly :" + deleteIndexResponse.isAcknowledged());
  }
}
