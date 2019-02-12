package com.esspnews.controller;

import com.esspnews.utils.EsUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MultiMatchQuery;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by bee on 17/8/10.
 */

@WebServlet(name = "/SearchNews", urlPatterns = "/SearchNews")
public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String query = req.getParameter("query");
        System.out.println(query);
        String pageNumStr=req.getParameter("pageNum");
        int pageNum=1;

        if (pageNumStr!=null&&Integer.parseInt(pageNumStr)>1){
            pageNum=Integer.parseInt(pageNumStr);
        }
        searchSpnews(query, pageNum,req);

        req.setAttribute("queryBack", query);
        req.getRequestDispatcher("result.jsp").forward(req, resp);

    }

    private void searchSpnews(String query, int pageNum,HttpServletRequest req) {

        long start = System.currentTimeMillis();
        TransportClient client = EsUtils.getSingleClient();
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders
                .multiMatchQuery(query, "title", "content");
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .preTags("<span style=\"color:red\">")
                .postTags("</span>")
                .field("title")
                .field("content");

        SearchResponse searchResponse = client.prepareSearch("spnews")
                .setTypes("news")
                .setQuery(multiMatchQuery)
                .highlighter(highlightBuilder)
                .setFrom((pageNum-1)*10)
                .setSize(10)
                .execute()
                .actionGet();

        SearchHits hits = searchResponse.getHits();
        ArrayList<Map<String, Object>> newslist = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : hits) {
            Map<String, Object> news = hit.getSourceAsMap();

            HighlightField hTitle = hit.getHighlightFields().get("title");
            if (hTitle != null) {
                Text[] fragments = hTitle.fragments();
                String hTitleStr = "";
                for (Text text : fragments) {
                    hTitleStr += text;
                }
                news.put("title", hTitleStr);
            }

            HighlightField hContent = hit.getHighlightFields().get("content");
            if (hContent != null) {
                Text[] fragments = hContent.fragments();
                String hContentStr = "";
                for (Text text : fragments) {
                    hContentStr += text;
                }
                news.put("content", hContentStr);
            }
            newslist.add(news);
        }
        long end = System.currentTimeMillis();
        req.setAttribute("newslist", newslist);
        req.setAttribute("totalHits", hits.getTotalHits() + "");
        req.setAttribute("totalTime", (end - start) + "");
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }


}
