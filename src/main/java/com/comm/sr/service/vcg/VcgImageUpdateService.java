package com.comm.sr.service.vcg;

import com.comm.sr.common.component.AbstractComponent;
import com.comm.sr.common.core.AbstractQueryService;
import com.comm.sr.common.elasticsearch.EsQueryService;
import com.comm.sr.common.entity.Image;
import com.comm.sr.common.kd.KDTree;
import com.comm.sr.common.kd.KeySizeException;
import com.comm.sr.common.utils.GsonHelper;
import com.comm.sr.common.utils.HttpUtils;
import com.comm.sr.service.ServiceUtils;
import com.comm.sr.service.cache.CacheService;
import com.comm.sr.service.topic.TopicService;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yufei.utils.CommonUtil;
import com.yufei.utils.ExceptionUtil;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by jasstion on 15/08/2017.
 */
public class VcgImageUpdateService extends AbstractComponent {
    protected AbstractQueryService queryService = null;


    public static class ImageUpdateParams {
        private String clusterIndentity = "vcgImage";
        private String imageIndexName = "vcg_image";
        private List<Image> images = Lists.newArrayList();




        public String getClusterIndentity() {
            return clusterIndentity;
        }

        public void setClusterIndentity(String clusterIndentity) {
            this.clusterIndentity = clusterIndentity;
        }

        public String getImageIndexName() {
            return imageIndexName;
        }

        public void setImageIndexName(String imageIndexName) {
            this.imageIndexName = imageIndexName;
        }

        public List<Image> getImages() {
            return images;
        }

        public void setImages(List<Image> images) {
            this.images = images;
        }
    }

    protected CacheService<String, String> cacheService = null;
    protected TopicService bytesTopicService = null;

    public VcgImageUpdateService(Properties settings, AbstractQueryService queryService, CacheService<String, String> cacheService, TopicService bytesTopicService) {
        super(settings);
        this.cacheService = cacheService;
        this.queryService = queryService;

        this.bytesTopicService = bytesTopicService;


    }



    public void batchAddImages(String updateParamsStr) throws Exception {
        ImageUpdateParams updateParams = (ImageUpdateParams) GsonHelper
                .jsonToObj(updateParamsStr, ImageUpdateParams.class);
        List<Image> images = updateParams.getImages();
        if(updateParams.getImageIndexName().equals("vcg_image")){
            images.parallelStream().forEach(new Consumer<Image>() {
                @Override
                public void accept(Image image) {
                    //创意类id - 60000000,   编辑不变
                    image.setImageId(String.valueOf((Long.parseLong(image.getImageId())-600000000)));

                }

            });

        }



        int maxBatchImageProcessLimit = Integer.parseInt(settings.getProperty("maxBatchImageProcessLimit","5"));
        if (images.size() > maxBatchImageProcessLimit) {
            throw new Exception("batchImageNumber big then maxImageNumberLimit!");
        }


        String addedImageTopicName = settings.getProperty("addedImageTopicName");

        logger.info("start to batch add new  pictures, batchSize:" + images.size() + "");
        Stopwatch stopwatch = Stopwatch.createStarted();


        final List<List<String>> imagesTriple = Lists.newArrayList();
        images.parallelStream().forEach(new Consumer<Image>() {
            @Override
            public void accept(Image image) {
                String url = image.getUrl();
                String imageId = image.getImageId();
                try {
                    String redisImageId = fetchImageAndPushToKafka(url, addedImageTopicName);
                    List<String> triple = Lists.newArrayList();//0:imageId,1:redisImageId,2:features,3:groupId
                    triple.add(0, imageId);
                    triple.add(1, redisImageId);
                    imagesTriple.add(triple);


                } catch (Exception e) {
                    logger.warn("error to fetch " + url + ", errors:" + ExceptionUtil.getExceptionDetailsMessage(e) + "");

                }


            }
        });

        int batchImageFeatureProcessWaitTime = Integer.parseInt(this.settings.getProperty("batchImageFeatureProcessWaitTime","10"));


        Thread.currentThread().sleep(batchImageFeatureProcessWaitTime * 1000);
        CacheService<String, String> redisCacheService = ServiceUtils.getCacheService();

        imagesTriple.parallelStream().forEach(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) {
                String features = redisCacheService.get(strings.get(1));
                List<String> doubleStrs=Lists.newArrayList(features.split(","));

                List<String> ds = doubleStrs.parallelStream().map(va -> String.valueOf(Math.round(Double.parseDouble(va) * 1000) / 1000.00)).collect(Collectors.toList());
                features = CommonUtil.LinkStringWithSpecialSymbol(ds, ",");
                strings.add(2, features);
            }
        });

        //process images features and generate image groupId then push to es

        computeGroupId(imagesTriple, updateParams.getImageIndexName());


        EsQueryService esQueryService = (EsQueryService) queryService;
        TransportClient transportClient = esQueryService.getEsClient(updateParams.getClusterIndentity());


        final List<Map<String, String>> messages = Lists.newArrayList();
        ;


        imagesTriple.forEach(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) {

                String imageId = strings.get(0);
                String redisImaageId = strings.get(1);
                String features = strings.get(2);
                String groupId = strings.get(3);
                Map<String, String> map = null;
                map = Maps.newHashMap();
                map.put("index", updateParams.getImageIndexName());
                map.put("type", "image");
                map.put("id", imageId);
                map.put("imageId",imageId);
                map.put("cNNFeatures", features);
                //通过接口形式添加
                map.put("addedWay","API");
                map.put("groupId", groupId);
                messages.add(map);


            }
        });


        BulkResponse bulkResponse = bulkUpdate(messages, transportClient);
        if (bulkResponse.hasFailures()) {
            throw new Exception(bulkResponse.buildFailureMessage());
        }
        //logger.info("succeed to update groupId:"+groupId+", num:"+messages.size()+"");


        stopwatch.stop();
        long timeSeconds = stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000;

        logger.info("finish to batch add pictures, batchSize:" + images.size() + ", taken time: " + timeSeconds + " s");


    }

    public static BulkResponse bulkUpdate(List<Map<String, String>> updatedMaps, Client client_vcgImage) throws Exception {

        BulkRequestBuilder bulkRequest = client_vcgImage.prepareBulk();

        for (Map<String, String> updateMap : updatedMaps) {
            String id = (String) updateMap.get("id");
            String type = updateMap.get("type");
            String index = updateMap.get("index");
            if (id == null || type == null || index == null) {
                continue;

            }
            updateMap.remove("id");
            updateMap.remove("type");
            updateMap.remove("index");


            //IndexRequestBuilder requestBuilder=client_vcgImage.prepareIndex().setId(id).setSource(finalUpdatedMap).setIndex(index).setType(type);
            // UpdateRequestBuilder requestBuilder=client_vcgImage.prepareUpdate().setId(id).setIndex(index).setType(type).setUpsert(updatedMaps);
            IndexRequest indexRequest = new IndexRequest(index, type, id).source(updateMap);
            UpdateRequest updateRequest = new UpdateRequest(index, type, id).doc(updateMap).upsert(indexRequest);
            bulkRequest.add(updateRequest);


        }
        BulkResponse bulkResponse = bulkRequest.get();
        return bulkResponse;


    }

    final KDTree<Integer> kdTree = new KDTree<Integer>(2048);

    private List<List<String>> computeGroupId(List<List<String>> imagesTriple, String indexName) throws Exception {


        if (kdTree.size() == 0) {

            String imageCenterVectorKeyPrefix = settings.getProperty("image.centerVectorKeyPrefix");
            Integer clusterNum = Integer.parseInt(settings.getProperty(indexName + ".image.cluster.num"));
            String preKey=imageCenterVectorKeyPrefix+indexName+"_";

            for (int i = 0; i < clusterNum; i++) {
                String key = preKey + i;
                String vecStr = cacheService.get(key);
                double[] vec = Lists.newArrayList(vecStr.split(",")).parallelStream()
                        .mapToDouble(va -> Double.parseDouble(va)).toArray();
                kdTree.insert(vec, i);


            }

        }
        imagesTriple.parallelStream().forEach(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) {
                String features = strings.get(2);
                double[] vec = Lists.newArrayList(features.split(",")).parallelStream()
                        .mapToDouble(va -> Double.parseDouble(va)).toArray();
                try {
                    String groupIdStr = String.valueOf(kdTree.nearest(vec));



                    strings.add(3, groupIdStr);
                } catch (KeySizeException e) {
                    logger.info("error to compute iamge groupId, " + ExceptionUtil.getExceptionDetailsMessage(e) + "");
                }


            }
        });

        return imagesTriple;

    }


    private String fetchImageAndPushToKafka(String imageUrl, String addedImageTopicName) throws IOException {
        String imageId = UUID.randomUUID().toString();
        TopicService topicBytesService = ServiceUtils.getByteTopicService();
        Stopwatch stopwatch = Stopwatch.createStarted();


        //byte[] imageBytes= HttpUtils.executeWithHttpImageUrl(searchParams.getMatchPictureUrl(),null);

        URL url = new URL(imageUrl);

        byte[] imageBytes = IOUtils.toByteArray(url.openStream());
        stopwatch.stop();
        long timeSeconds = stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000;
        logger.info("spent " + timeSeconds + " s to fetch " + imageUrl + "");
        topicBytesService.publishTopicMessage(addedImageTopicName, imageId.getBytes(), imageBytes);

        return imageId;
    }

    public static void main(String[] args){




        ImageUpdateParams imageUpdateParams=new ImageUpdateParams();
        imageUpdateParams.setClusterIndentity("vcgImage");
        imageUpdateParams.setImageIndexName("vcg_image");

        List<Image> images=Lists.newArrayList();
        Image image=new Image();
        image.setImageId("10000000000");
        image.setUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502785015423&di=bb72cb9052e4fbfd616ee2d94ebfff2f&imgtype=0&src=http%3A%2F%2Fimgcc.12584.cn%2F2017080310291927.jpg");
        Image image1=new Image();
        image1.setImageId("10000000001");
        image1.setUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1502785015423&di=bb72cb9052e4fbfd616ee2d94ebfff2f&imgtype=0&src=http%3A%2F%2Fimgcc.12584.cn%2F2017080310291927.jpg");

        images.add(image);
        images.add(image1);

        imageUpdateParams.setImages(images);
        String params=GsonHelper.objToJson(imageUpdateParams);
        System.out.println(params+"\n");



        String requestUrl="http://60.205.226.115:8080/inner/srservice/addImagesByUrl.json";
        Map<String,Object> paramsMap=Maps.newHashMap();
        paramsMap.put("params",params);
       String result= HttpUtils.executeWithHttp(requestUrl,paramsMap);
       System.out.println(result);


    }

}
