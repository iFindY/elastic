package de.arkadi.elasticsearch.twitter;

import de.arkadi.elasticsearch.kafka.KafkaProducerResult;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterKafkaServiceTesting {

  private TwitterStream twitterStream;

  public TwitterKafkaServiceTesting(ConfigurationBuilder configuration,
                                    KafkaProducerResult kafkaProducer) {

    FilterQuery tweetFilterQuery = new FilterQuery();
    twitterStream = new TwitterStreamFactory(configuration.build()).getInstance();
    twitterStream.addListener(new ProducerTesting(kafkaProducer));
    tweetFilterQuery.track("London", "Berlin", "Moscow");
    tweetFilterQuery.language("en");
    streamToKafka(tweetFilterQuery);

  }

  private void streamToKafka(FilterQuery filterQuery) {

    System.out.println("TWEETER STREAM STARTED");
    twitterStream.filter(filterQuery);
  }

}
