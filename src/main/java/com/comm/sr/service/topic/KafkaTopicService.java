package com.comm.sr.service.topic;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.service.ServiceUtils;
import com.yufei.utils.ExceptionUtil;
import com.yufei.utils.IOUtils;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by jasstion on 27/10/2016.
 */

public class KafkaTopicService extends AbstractComponent implements TopicService {

    private Properties properties=new Properties();
    private Producer<String,String> producer=null;


    public KafkaTopicService(Properties settings) {
        super(settings);
        this.properties = settings;
        ProducerConfig config = new ProducerConfig(properties);

        producer = new Producer<String, String>(config);
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
            KeyedMessage<String, String> data =
                new KeyedMessage<String, String>(topic, null, message);

            producer.send(data);
            if (logger.isDebugEnabled()) {
                logger.debug("succeed to send message[{}]", data);
            }
        }catch (Exception e){

            logger.info("error to send message to kafka!, exception message:"+ ExceptionUtil.getExceptionDetailsMessage(e)+"");
        }

    }
    @Override
    public void publishTopicMessage(String topic,byte[] key,byte[] message){
        producer.send(new KeyedMessage(topic, key,message));

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        producer.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Properties props=new Properties();
        props.put("metadata.broker.list", "localhost:9092");
       // props.put("serializer.class", "kafka.serializer.StringEncoder");

       // props.put("request.required.acks", "1");
        byte[] bytes= IOUtils.translantStreamToByte(new FileInputStream(new File(
            "/data/mlib_data/images/images_test/vcg_creative/201273662.jpg")));

        //Producer producer = new Producer<String, String>(new ProducerConfig(props));
        //producer.send(new KeyedMessage("image_upload", "4534460263_8e9611db3c_n".getBytes(),bytes));
        //TopicService topicService=new KafkaTopicService(props);
        TopicService topicService= ServiceUtils.getByteTopicService();
        topicService.publishTopicMessage("image_upload", "1".getBytes(),bytes);







    }
}
