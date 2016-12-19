
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Created by jasstion on 15/6/29.
 */
public class SolrCloudTest {

  public static void main(String[] args) throws Exception {
    CloudSolrServer cloudSolr = null;// = CloudSolr.getWomanInstance();
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery("*:*");

    QueryResponse queryResponse = cloudSolr.query(solrQuery);
    System.out.println(queryResponse.getResponse().toString());
    int totalCount = Integer.parseInt((String) queryResponse.getResponse().get("numFound"));
    System.out.print(totalCount);

  }

}
