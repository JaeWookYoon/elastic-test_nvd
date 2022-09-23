package com.seekerslab.nvd.es;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import org.apache.lucene.queryparser.flexible.standard.parser.ParseException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@SuppressWarnings("resource")
public class ElasticsearchConnection {
	
	private Logger logger = LoggerFactory.getLogger(getClass());	
	private Settings setting = Settings.builder().put("cluster.name", "sks").put("node.name", "sks-master") // elastic search 정보 Setting
			.put("node.name", "sks-data1").put("node.name", "sks-data2")//cluster정보 및 마스터,데이터 노드 등록
			.build();
	private TransportClient client;
	{//블럭 초기화 실행
		try {
			client = new PreBuiltTransportClient(setting) // setting값으로 부터 client 객체 생성
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("172.16.100.251"), 9300)); // elastic master node 정보
					// 9200 - elastic data 확인 port , 9300 transport client 사용 port
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());	
			logger.info("모듈?");
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean indexExist(String year) {// index가 존재할 경우, 여부 확인			
		ImmutableOpenMap map =client.admin().cluster().state(new ClusterStateRequest()).actionGet().getState().getMetaData().getIndices();
		boolean ex = false;
		for(int i=0;i<map.size();i++) {// map형태로 index name list 출력
			ex = map.containsKey("cve-"+year);						
		}
		logger.info(String.valueOf(ex));
		return ex;
	}
	@SuppressWarnings("unused")
	public void deleteIndex(String year) {// 해당하는 year의 index삭제		
		try {			
			DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest("cve-"+year)).actionGet();// 삭제 실행
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	@Deprecated
	public void insertDatas(String str,String year) throws org.json.simple.parser.ParseException, IOException {
		JSONObject obj = new JSONObject();
		JSONParser parser = new JSONParser();
		IndexRequest response;
						
		BulkRequestBuilder bulkRequest = client.prepareBulk();//client의 bulk index 준비		
		BulkResponse bulkResponse = null;
		StringBuffer builder = new StringBuffer();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(str))));
		try {
			String strs = "";
			
			while ((strs = br.readLine()) != null) {	//읽어온 json 파일을 stringbuffer로 읽어옵니다.						
				builder.append(strs);				
			}
		} catch (IOException ie) {
			logger.error(ie.getMessage());
		}
		
		Object obje = parser.parse(builder.toString());//stringbuffer로 읽은 json데이터를  파싱합니다.
		obj = (JSONObject) obje;
		JSONArray arr = (JSONArray) obj.get("CVE_Items");//가장 큰 카테고리 CVE_Items를 가져옵니다.
	
		XContentBuilder build = XContentFactory.jsonBuilder().startObject();//startObject 는 json의 {
		/***************************************** 우선 기본 설정 데이터 bulk index **********************************************/
		build.field("CVE_data_type", obj.get("CVE_data_type"));
		build.field("CVE_data_format", obj.get("CVE_data_format"));
		build.field("CVE_data_version", obj.get("CVE_data_version"));
		build.field("CVE_data_numberOfCVEs", obj.get("CVE_data_numberOfCVEs"));
		build.field("CVE_data_timestamp", obj.get("CVE_data_timestamp"));
		build.field("collect_date", new Date(System.currentTimeMillis()));
		build.endObject();//endObject 는 
		response = new IndexRequest("cve-" + year, "nvdcve").source(build);
		
		bulkRequest.add(response);
		/***************************************** 우선 기본 설정 데이터 bulk index **********************************************/
		
		XContentBuilder build1 = null;		
		for (int i = 0; i < arr.size(); i++) {		//json파일 	
			build1 = XContentFactory.jsonBuilder().startObject();//Start XBuilder

			JSONObject a = (JSONObject) arr.get(i);
			
			build1.startObject("CVE_ITEMS");//Start CVE-Items
			/********************************* 5가지 큰 Category로 field 삽입 *******************************************/
			//각각의 Category들의 하위 카테고리들은 자동 json파싱되어 트리구조가 형성됩니다.
			build1.field("cve", a.get("cve"));
			build1.field("configurations", a.get("configurations"));
			build1.field("impact", a.get("impact"));
			build1.field("publishedDate", a.get("publishedDate"));
			build1.field("lastModifiedDate", a.get("lastModifiedDate"));
			/********************************* 5가지 큰 Category로 field 삽입 *******************************************/
			build1.endObject();// end cve
			build1.endObject();// end XBuilder
					
			response = new IndexRequest("cve-" + year, "nvdcve").source(build1);//type : nvdcve , index : cve- 해당 년도 
									
			bulkResponse = bulkRequest.add(response).get();//bulk index 실행						
		}				
		br.close();		
	}	
	public void insertData(String fileAddress, String year) throws UnknownHostException, ParseException, IOException,
			java.text.ParseException, org.json.simple.parser.ParseException {		
		JSONObject obj = new JSONObject();
		JSONParser parser = new JSONParser();
		IndexRequest response;		
		BulkProcessor bulk = getBulk(client, 10000);		
		StringBuffer builder = new StringBuffer();		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileAddress))));//json 파일 read
		try {
			String strs = "";
			
			while ((strs = br.readLine()) != null) {							
				builder.append(strs);	//json 파일 데이터를 stringbuffer에 담음			
			}
		} catch (IOException ie) {
			logger.error(ie.getMessage());
		}		
		Object obje = parser.parse(builder.toString());
		obj = (JSONObject) obje;
		JSONArray arr = (JSONArray) obj.get("CVE_Items");
	
		XContentBuilder build = XContentFactory.jsonBuilder().startObject();
		/***************************************** 우선 기본 설정 데이터 bulk index **********************************************/
		build.field("CVE_data_type", obj.get("CVE_data_type"));
		build.field("CVE_data_format", obj.get("CVE_data_format"));
		build.field("CVE_data_version", obj.get("CVE_data_version"));
		build.field("CVE_data_numberOfCVEs", obj.get("CVE_data_numberOfCVEs"));
		build.field("CVE_data_timestamp", obj.get("CVE_data_timestamp"));
		build.field("collect_date", new Date(System.currentTimeMillis()));
		build.endObject();
		response = new IndexRequest("cve-" + year, "nvdcve").source(build);

		bulk.add(response);
		/***************************************** 우선 기본 설정 데이터 bulk index **********************************************/
		
		XContentBuilder build1 = null;		
		for (int i = 0; i < arr.size(); i++) {			
			build1 = XContentFactory.jsonBuilder().startObject();//Start XBuilder
			JSONObject a = (JSONObject) arr.get(i);//Start cve
			
			build1.startObject("CVE_ITEMS");
			/********************************* 5가지 큰 Category로 field 삽입 *******************************************/
			//각각의 Category들의 하위 카테고리들은 자동 json파싱되어 트리구조가 형성됩니다.
			build1.field("cve", a.get("cve"));
			build1.field("configurations", a.get("configurations"));
			build1.field("impact", a.get("impact"));
			build1.field("publishedDate", a.get("publishedDate"));
			build1.field("lastModifiedDate", a.get("lastModifiedDate"));
			/********************************* 5가지 큰 Category로 field 삽입 *******************************************/
			build1.endObject();// end cve
			build1.endObject();// end XBuilder
					
			response = new IndexRequest("cve-" + year, "nvdcve").source(build1); // type : nvdcve , index : cve-해당년도
									
			bulk.add(response);			
		}		
		bulk.close();		
		br.close();		
	}

	@SuppressWarnings("unused")
	public void createIndex(String year) throws IOException {

		XContentBuilder build = XContentFactory.jsonBuilder().startObject().startObject("nvdcve")
				.startObject("properties");
		build.startObject("publishedDate");
		build.field("type", "date");
		build.endObject();// end publish
		build.startObject("lastModifiedDate");
		build.field("type", "date");
		build.endObject();// end lastModify
		build.endObject();// end properties

		build.endObject();// end type
		build.endObject();// end XBuilder
		IndexResponse response = client.prepareIndex("cve-" + year, "nvdcve").setSource(build).execute().actionGet();

	}

	public BulkProcessor getBulk(Client client, int bulkCount) {
		BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
			
			public void beforeBulk(long extensionId, BulkRequest request) {
				// TODO Auto-generated method stub
				request.numberOfActions();
				logger.info("beforeBuld");									
			}
			public void afterBulk(long extensionId, BulkRequest request, Throwable failure) {
				// TODO Auto-generated method stub
				request.numberOfActions();
				logger.info(failure.getMessage());
				failure.printStackTrace();
				logger.info("afterBulk");
			}
			public void afterBulk(long extensionId, BulkRequest request, BulkResponse response) {
				// TODO Auto-generated method stub
				request.numberOfActions();

				logger.info(String.valueOf(response.hasFailures()));
				logger.info(response.buildFailureMessage());
				logger.info("afterBulk");
			}
		}).setBulkActions(bulkCount).setBulkSize(new ByteSizeValue(3, ByteSizeUnit.GB))
				.setFlushInterval(TimeValue.timeValueSeconds(5)).setConcurrentRequests(10)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)).build();
		return bulkProcessor;
	}

	public long searchDocument() {						
		SearchResponse search = client.prepareSearch("cve-2002").setTypes("nvdcve")
				.setQuery(QueryBuilders.termQuery("CVE_ITEMS.cve.CVE_data_meta.ID", "CVE-1999-0019"))
				.setFrom(0).setSize(9000).setExplain(true)
				.get();
		SearchHits hit = search.getHits();
		for(int i=0;i<hit.totalHits;i++) {
			SearchHit hi = hit.getAt(i);
			Map<String,Object>map = hi.getSource();
			JSONObject obj = new JSONObject(map);
			System.out.println(obj.toJSONString());
		}
		logger.info(String.valueOf(search.getHits().totalHits) + "lognow");
		return search.getHits().totalHits;		
	}	
}
