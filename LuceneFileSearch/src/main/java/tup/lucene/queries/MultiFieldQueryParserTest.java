package tup.lucene.queries;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
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

public class MultiFieldQueryParserTest {

	public static void main(String[] args) throws IOException, ParseException {
		String[] fields = { "title", "content" };

		Path indexPath = Paths.get("indexdir");
		Directory dir = FSDirectory.open(indexPath);
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new IKAnalyzer6x(true);

		MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
		Query multiFieldQuery = parser.parse("日本");
		System.out.println(multiFieldQuery.toString());

		// 返回前10条
		TopDocs tds = searcher.search(multiFieldQuery, 10);
		for (ScoreDoc sd : tds.scoreDocs) {
			// Explanation explanation = searcher.explain(query, sd.doc);
			// System.out.println("explain:" + explanation + "\n");
			Document doc = searcher.doc(sd.doc);
			System.out.println("DocID:" + sd.doc);
			System.out.println("id:" + doc.get("id"));
			System.out.println("title:" + doc.get("title"));
			System.out.println("content:" + doc.get("content"));
			System.out.println("文档评分:" + sd.score);
		}
		dir.close();
		reader.close();
	}

}
