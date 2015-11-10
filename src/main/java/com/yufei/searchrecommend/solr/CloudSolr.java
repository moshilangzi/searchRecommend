/**
 * 
 */
package com.yufei.searchrecommend.solr;

import com.yufei.searchrecommend.utils.ReadProperties;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * @author crazyicele
 * @updated zhaoyf
 * 
 */
public class CloudSolr {
	private static Logger logger=Logger.getLogger(CloudSolr.class);
	private static String zkHost= ReadProperties.appBundle.getString("solrcloud.zkHost");
	private static int max_connections = Integer.parseInt(ReadProperties.appBundle.getString("solrcloud.max_connections"));
	private static int max_connections_per_host = Integer.parseInt(ReadProperties.appBundle.getString("solrcloud.max_connections_per_host"));
	private static int zkConnectTimeout = Integer.parseInt(ReadProperties.appBundle.getString("solrcloud.zkConnectTimeout"));
	private static int zkClientTimeout= Integer.parseInt(ReadProperties.appBundle.getString("solrcloud.zkClientTimeout"));
	private static String manCollection = "yufeiSearch_man";
	private static String womanCollection = "yufeiSearch_woman";
	private static CloudSolrServer manSolrServer;
	private static CloudSolrServer womanSolrServer;



	public static synchronized CloudSolrServer getManInstance() {
		if (manSolrServer == null) {
			try {
				ModifiableSolrParams params = new ModifiableSolrParams();
				params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, max_connections);
				params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST,max_connections_per_host);
				HttpClient client = HttpClientUtil.createClient(params);
				LBHttpSolrServer lbServer = new LBHttpSolrServer(client);
				manSolrServer = new CloudSolrServer(zkHost, lbServer);
				manSolrServer.setZkConnectTimeout(zkConnectTimeout);
				manSolrServer.setZkClientTimeout(zkClientTimeout);
				manSolrServer.setDefaultCollection(manCollection);
				logger.info("初始化manSolrServer成功！");
			} catch (Exception e) {
				logger.error("初始化manSolrServer失败");

			}
		}
		return manSolrServer;
	}
	public static synchronized CloudSolrServer getWomanInstance() {
		if (womanSolrServer == null) {
			try {
				ModifiableSolrParams params = new ModifiableSolrParams();
				params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, max_connections);
				params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST,max_connections_per_host);
				HttpClient client = HttpClientUtil.createClient(params);
				LBHttpSolrServer lbServer = new LBHttpSolrServer(client);
				womanSolrServer = new CloudSolrServer(zkHost, lbServer);
				womanSolrServer.setZkConnectTimeout(zkConnectTimeout);
				womanSolrServer.setZkClientTimeout(zkClientTimeout);
				womanSolrServer.setDefaultCollection(womanCollection);
				logger.info("初始化womanSolrServer成功！");
			} catch (Exception e) {
				logger.error("初始化womanSolrServer失败");
			}
		}
		return womanSolrServer;
	}
}
