package com.esspnews.utils;

import com.esspnews.dao.Dao;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;

import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;

import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsUtils {

    public static final String CLUSTER_NAME = "elasticsearch";
    public static final String HOST_IP = "127.0.0.1";
    public static final int TCP_PORT = 9300;
    static Settings settings = Settings.builder()
            .put("cluster.name", CLUSTER_NAME)
            .build();

    public static TransportClient getClient() {
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(  //注意这里和前面版本不一样
                            InetAddress.getByName(HOST_IP), TCP_PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return client;
    }

    private static volatile TransportClient client;

    //单例模式
    public static TransportClient getSingleClient() {
        if (client == null) {
            synchronized (TransportClient.class) {

                if (client == null) {
                    client=getClient();
                  /*  try {
                        client = new PreBuiltTransportClient(settings)
                                .addTransportAddress(new InetSocketTransportAddress(
                                        InetAddress.getByName(HOST_IP), TCP_PORT));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }*/

                }

            }

        }
        System.out.println(client);
        return client;
    }

    //获取索引管理的IndicesAdminClient
    public static IndicesAdminClient getAdminClient() {


        return getSingleClient().admin().indices();
    }

    //创建索引
    public static boolean createIndex(String indexName, int shards, int replicas) {

        Settings settings = Settings.builder()
                .put("index.number_of_shards", shards)
                .put("index.number_of_replicas", replicas)
                .build();

        CreateIndexResponse createIndexResponse = getAdminClient()
                .prepareCreate(indexName.toLowerCase())
                .setSettings(settings)
                .get();

        boolean isIndexCreated = createIndexResponse.isAcknowledged();
        if (isIndexCreated) {
            System.out.println("索引" + indexName + "创建成功");
        } else {
            System.out.println("索引" + indexName + "创建失败");
        }


        return isIndexCreated;


    }

    public static boolean deleteIndex(String indexName) {

        /*DeleteResponse deleteResponse = getAdminClient()
                .prepareDelete(indexName.toLowerCase())
                .execute()
                .actionGet();*/
        AcknowledgedResponse deleteResponse = getAdminClient()
                .prepareDelete(indexName.toLowerCase())
                .get();

        boolean isIndexDeleted = deleteResponse.isAcknowledged();

        if (isIndexDeleted) {
            System.out.println("索引" + indexName + "删除成功");
        } else {
            System.out.println("索引" + indexName + "删除失败");
        }

        return isIndexDeleted;
    }

    public static boolean setMapping(String indexName, String typeName, String mapping) {


        getAdminClient().preparePutMapping(indexName)
                .setType(typeName)
                .setSource(mapping, XContentType.JSON)
                .get();


        return false;
    }



    public static void main(String[] args) {

        //1.创建索引，如果已经创建过就回报错

        EsUtils.createIndex("spnews", 3, 0);

        //2.设置Mapping
        try {
            XContentBuilder builder = jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("id")
                    .field("type", "long")
                    .endObject()
                    .startObject("title")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_max_word")
                    .field("boost", 2)
                    .endObject()
                    .startObject("key_word")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_max_word")
                    .endObject()
                    .startObject("content")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_max_word")
                    .endObject()
                    .startObject("url")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("reply")
                    .field("type", "long")
                    .endObject()
                    .startObject("source")
                    .field("type", "keyword")
                    .endObject()
                    .startObject("postdate")
                    .field("type", "date")
                    .field("format", "yyyy-MM-dd HH:mm:ss")
                    .endObject()
                    .endObject()
                    .endObject();

            System.out.println(builder.toString());

            EsUtils.setMapping("spnews", "news", builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //3.  读取MySQL

        Dao dao = new Dao();
        dao.getConnection();
        dao.mysqlToEs();


    }
}
