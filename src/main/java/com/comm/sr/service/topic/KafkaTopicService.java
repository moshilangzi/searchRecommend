package com.comm.sr.service.topic;

import java.util.Properties;

import com.comm.sr.common.component.AbstractComponent;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * Created by jasstion on 27/10/2016.
 */
public class KafkaTopicService extends AbstractComponent implements TopicService {

  private Properties properties = new Properties();
  private Producer<String, String> producer = null;

  public KafkaTopicService(Properties settings) {
    super(settings);
    this.properties = settings;
    ProducerConfig config = new ProducerConfig(properties);

    producer = new Producer<String, String>(config);
  }

  public static void main(String[] args) {
    Properties props = new Properties();
    props.put("metadata.broker.list", "localhost:9092");
    props.put("serializer.class", "kafka.serializer.StringEncoder");

    // props.put("request.required.acks", "1");
    TopicService topicService = new KafkaTopicService(props);
    topicService.publishTopicMessage("my-replicated-topic", "this is my first message!");

  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public void publishTopicMessage(String topic, String message) {
    try {
      KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, null, message);

      producer.send(data);
      if (logger.isDebugEnabled()) {
        logger.debug("succeed to send message[{}]", data);
      }
    } catch (Exception e) {
      logger.info("error to send message to kafka!");
    }

  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    producer.close();
  }
}
