
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.Update;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jasstion
 */
public class JestMain {

       final static  String esMasterUrl = ResourceBundle.getBundle("elasticSearch").getString("elasticSearchUrl");

    public static JestClient client = null;

    static {
        HttpClientConfig clientConfig = new HttpClientConfig.Builder(esMasterUrl).multiThreaded(true).build();
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(clientConfig);
        client = factory.getObject();
    }

    public static void commTest() throws IOException {
        // String source =XContentFactory.jsonBuilder().startObject().field("height", 198).field("age", 1988).endObject().toString();
        Map<String, Object> source = Maps.newHashMap();
        source.put("realName", "zhaoyufei");
        source.put("nickname", "jasstion");
        source.put("height", 187);
        source.put("age", 1988);
        source.put("userID", 5672334);

        Index index = new Index.Builder(source).index("comm").type("user").id("5672334").build();
        client.execute(index);

        source.clear();
        source.put("realName", "zhaoyufei");
        source.put("nickname", "jasstion");
        source.put("height", 187);
        source.put("age", 1988);
        source.put("userID", 567234);
        source.put("id", 567234);

        index = new Index.Builder(source).index("comm").type("user").build();
        client.execute(index);

    }

    public static void main(String[] args) throws Exception {

        testUpdateDate();

    }

    public static void testUpdateDate() throws IOException, Exception {
//        List<QueryItem> queryItems = Lists.newArrayList();
//        QueryItem queryItem = new QueryItem("id", Lists.newArrayList("AVHx2cBqXMEzvqfnenVE"));
//        queryItems.add(queryItem);
//        EsCommonQuery escommQuery = new EsCommonQuery(queryItems, 1, 10, Lists.newArrayList(), Lists.newArrayList("age", "height"), "comm", "user");
//        EsQueryService eqs = new EsQueryService();
//        List<Map<String, Object>> result = eqs.query(escommQuery);
//        System.out.println(result.size());
//        Map<String, Object> source = Maps.newHashMap();
//        source.put("registeDate", new Date());
//        source.put("id", 567234);

//        Map<String, Object> source = Maps.newHashMap();
//
//        source.put("script", "ctx._source.city=city;ctx._source.age=age");
//        String params="{city: 864109,age:1988}";
//        source.put("params", params);
//        String source_json=JSON.toJSONString(source, SerializerFeature.WriteMapNullValue);
//        System.out.print(source_json+"\n");
        Map<String, String> updatedMap = Maps.newHashMap();
        updatedMap.put("age", "1991");
        updatedMap.put("height", "1991");
        updatedMap.put("nickname", "jasstion");
      //  updatedMap.put("id", "999");
        
        String source_json = generateEsUpdateScriptFromMap(updatedMap);
        String updateScript =  "{\"script\" : \"ctx._source.nickname=nickname;ctx._source.city=city; ctx._source.height=height;ctx._source.age=age\",\"params\" : {\"city\": 864102,\"age\":1988,\"height\":199, \"nickname\":\"jasstion1\"},\"upsert\":{\"id\":\"99999\"}}";//
        Update update = new Update.Builder(updateScript).index("comm").type("user").id("111111").build();
       // client.execute(update);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.idsQuery("user").ids("111111"));
        Search search = (Search) new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added..
                .addIndex("comm")
                .build();
        JestResult result1 = client.execute(search);
        System.out.println(result1.getJsonString());

    }

    public static String generateEsUpdateScriptFromMap(Map<String, String> updatesMap) {
        JsonObject jsonObj = new JsonObject();
        StringBuffer scriptBuffer = new StringBuffer();
        JsonObject jsonObject_1 = new JsonObject();
        for (Map.Entry<String, String> entrySet : updatesMap.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            scriptBuffer.append("ctx._source.").append(key).append("=" + key + ";");
            jsonObject_1.addProperty(key, value);

        }
        jsonObj.addProperty("script", scriptBuffer.toString());

        jsonObj.add("params", jsonObject_1);
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonObj);

    }

}
