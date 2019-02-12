package searchdemo;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.wltea.analyzer.lucene.IKAnalyzer;
import util.JdbcUtil;


import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.sql.ResultSet;

public class DbSearchDemo
{
    public static final String INDEX_PATH = "E:\\lucene-db";
    public static final String JDBC_URL = "jdbc:mysql://localhost:3306/lucene-demo?useUnicode=true&characterEncoding=utf-8";
    public static final String USER = "root";
    public static final String PWD = "root";

    /**
     * 创建索引
     */
    public void creatIndex()
    {
        IndexWriter indexWriter = null;
        try
        {
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_PATH));
            /**
             * represents a policy for extracting index terms from text.
             * 具体方法有：KeywordAnalyzer, PerFieldAnalyzerWrapper, SimpleAnalyzer, StandardAnalyzer, StopAnalyzer, WhitespaceAnalyzer
             * */
            //Analyzer analyzer = new StandardAnalyzer();
            Analyzer analyzer = new IKAnalyzer(true);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            indexWriter.deleteAll();// 清除以前的index

            JdbcUtil jdbc = new JdbcUtil();
            ResultSet rs = jdbc.query("select * from blog");
            while(rs.next())
            {
                Document document = new Document();
                document.add(new Field("id", rs.getString("id"), TextField.TYPE_STORED));
                document.add(new Field("title", rs.getString("title"), TextField.TYPE_STORED));
                document.add(new Field("content", rs.getString("content"), TextField.TYPE_STORED));
                document.add(new Field("tag", rs.getString("tags"), TextField.TYPE_STORED));
                document.add(new Field("url", rs.getString("url"), TextField.TYPE_STORED));
                indexWriter.addDocument(document);
            }
            jdbc.closeAll();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(indexWriter != null) indexWriter.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 搜索
     */
    public void search(String keyWord)
    {
        DirectoryReader directoryReader = null;
        try
        {
            // 1、创建Directory
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_PATH));
            // 2、创建IndexReader
            directoryReader = DirectoryReader.open(directory);
            // 3、根据IndexReader创建IndexSearch
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
            // 4、创建搜索的Query
            // Analyzer analyzer = new StandardAnalyzer();
            Analyzer analyzer = new IKAnalyzer(true); // 使用IK分词

            // 简单的查询，创建Query表示搜索域为content包含keyWord的文档
            //Query query = new QueryParser("content", analyzer).parse(keyWord);

            String[] fields = {"title", "content", "tag"};
            // MUST 表示and，MUST_NOT 表示not ，SHOULD表示or
            BooleanClause.Occur[] clauses = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
            // MultiFieldQueryParser表示多个域解析， 同时可以解析含空格的字符串，如果我们搜索"上海 中国"
            Query multiFieldQuery = (Query) MultiFieldQueryParser.parse(keyWord, fields, clauses, analyzer);

            // 5、根据searcher搜索并且返回TopDocs
            TopDocs topDocs = indexSearcher.search(multiFieldQuery, 100); // 搜索前100条结果
            System.out.println("共找到匹配处：" + topDocs.totalHits);
            // 6、根据TopDocs获取ScoreDoc对象
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            System.out.println("共找到匹配文档数：" + scoreDocs.length);

            QueryScorer scorer = new QueryScorer(multiFieldQuery, "content");
            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<span style=\"backgroud:red\">", "</span>");
            Highlighter highlighter = new Highlighter(htmlFormatter, scorer);
            highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
            for (ScoreDoc scoreDoc : scoreDocs)
            {
                // 7、根据searcher和ScoreDoc对象获取具体的Document对象
                Document document = indexSearcher.doc(scoreDoc.doc);
                String content = document.get("content");
                //TokenStream tokenStream = new SimpleAnalyzer().tokenStream("content", new StringReader(content));
                //TokenSources.getTokenStream("content", tvFields, content, analyzer, 100);
                //TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), scoreDoc.doc, "content", document, analyzer);
                //System.out.println(highlighter.getBestFragment(tokenStream, content));
                System.out.println("-----------------------------------------");
                System.out.println("文章标题："+document.get("title"));
                System.out.println("文章地址：" + document.get("url"));
                System.out.println("文章内容：");
                System.out.println(highlighter.getBestFragment(analyzer, "content", content));
                System.out.println("");
                // 8、根据Document对象获取需要的值
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(directoryReader != null) directoryReader.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * test tokenStream分词后的每个词属性
     * */
    public void testAttribute() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        String input = "This is a test text for attribute! Just add-some word.";
        TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(input));

        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
        PayloadAttribute payloadAttribute = tokenStream.addAttribute(PayloadAttribute.class);
        payloadAttribute.setPayload(new BytesRef("Just"));

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            System.out.print(
                    "[" + charTermAttribute
                            + " increment:" + positionIncrementAttribute.getPositionIncrement()
                            + " start:" + offsetAttribute.startOffset()
                            + " end:" + offsetAttribute.endOffset()
                            + " type:"+ typeAttribute.type()
                            + " payload:" + payloadAttribute.getPayload() + "]\n");
        }

        tokenStream.end();
        tokenStream.close();
    }
    public static void main(String args[]) throws IOException {

        DbSearchDemo demo = new DbSearchDemo();
        demo.testAttribute();
        //demo.creatIndex();
        //demo.search("android");
    }
}