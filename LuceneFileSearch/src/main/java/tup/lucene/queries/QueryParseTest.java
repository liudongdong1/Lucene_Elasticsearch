package tup.lucene.queries;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import tup.lucene.ik.IKAnalyzer6x;

public class QueryParseTest {

	public static void main(String[] args) throws ParseException, IOException {
		String field = "title";
		Path indexPath = Paths.get("indexdir");
		Directory dir = FSDirectory.open(indexPath);
		/**
		 * IndexReader is an abstract class, providing an interface for accessing
		 * a point-in-time view of an index. Any changes made to the index via IndexWriter
		 * will not be visible until a new IndexReader is opened. It's best to use
		 * DirectoryReader.open(IndexWriter,boolean) to obtain an IndexReader,
		 * if your IndexWriter is in-process. When you need to re-open to see changes to the index,
		 * it's best to use DirectoryReader.openIfChanged(DirectoryReader) since the new reader will
		 * share resources with the previous one when possible. Search of an index is done entirely through
		 * this abstract interface, so that any subclass which implements it is searchable.
		 * */
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new IKAnalyzer6x(true);
		QueryParser parser = new QueryParser(field, analyzer);
		parser.setDefaultOperator(Operator.AND);
		// 查询语句
		Query query = parser.parse("农村学生");
		System.out.println("Query:" + query.toString());
		
		/**
		 * search(Query query, Collector results)
		 * Lower-level search API.
		 * void	search(Query query, Filter filter, Collector results)
		 * Lower-level search API.
		 * TopDocs	search(Query query, Filter filter, int n)
		 * Finds the top n hits for query, applying filter if non-null.
		 * TopFieldDocs	search(Query query, Filter filter, int n, Sort sort)
		 * Search implementation with arbitrary sorting.
		 * TopFieldDocs	search(Query query, Filter filter, int n, Sort sort, boolean doDocScores, boolean doMaxScore)
		 * Search implementation with arbitrary sorting, plus control over whether hit scores and max score should be computed.
		 * TopDocs	search(Query query, int n)
		 * Finds the top n hits for query.
		 * TopFieldDocs	search(Query query, int n, Sort sort)
		 * Search implementation with arbitrary sorting and no filter.
		 * */
		// 返回前10条
		TopDocs tds = searcher.search(query, 10);
		for (ScoreDoc sd : tds.scoreDocs) {
			// Explanation explanation = searcher.explain(query, sd.doc);
			// System.out.println("explain:" + explanation + "\n");
			Document doc = searcher.doc(sd.doc);
			System.out.println("DocID:" + sd.doc);
			System.out.println("id:" + doc.get("id"));
			System.out.println("title:" + doc.get("title"));
			System.out.println("文档评分:" + sd.score);
		}
		dir.close();
		reader.close();
	}
}
