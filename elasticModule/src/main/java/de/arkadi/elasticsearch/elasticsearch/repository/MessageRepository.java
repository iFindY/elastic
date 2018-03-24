package de.arkadi.elasticsearch.elasticsearch.repository;

import de.arkadi.elasticsearch.model.Message;
import de.arkadi.elasticsearch.model.Request;
import de.arkadi.elasticsearch.model.Result;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

  public void createMapping() throws IOException {

    Settings indexSettings = Settings.builder()
      .put(SETTING_NUMBER_OF_SHARDS, 1)
      .put(SETTING_NUMBER_OF_REPLICAS, 0)
      .build();

    String payload = XContentFactory.jsonBuilder()
      .startObject()
      .startObject("settings")
      .value(indexSettings)
      .endObject()
      .startObject("mappings")
      .startObject("message")
      .startObject("properties")
      .startObject("message")
      .field("type", "text")
      .field("analyzer", "english")
      .endObject()
      .endObject()
      .endObject()
      .endObject()
      .endObject()
      .string();

    HttpEntity entity = new NStringEntity(payload, ContentType.APPLICATION_JSON);

    client.getLowLevelClient()
      .performRequest("PUT", "twitterindex", Collections.emptyMap(), entity);

  }

  public void dropIndex() {

  }
}
