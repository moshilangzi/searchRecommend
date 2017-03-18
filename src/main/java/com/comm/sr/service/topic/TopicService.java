package com.comm.sr.service.topic;

/**
 * Created by jasstion on 27/10/2016.
 */
public interface TopicService {


    public void publishTopicMessage(String topic,String message);
    public void publishTopicMessage(String topic,byte[] key,byte[] message);
}
