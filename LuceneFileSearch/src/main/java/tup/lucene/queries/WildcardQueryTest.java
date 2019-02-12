package tup.lucene.queries;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class WildcardQueryTest {
	public static void main(String[] args) throws IOException {
		String field = "content";
		Path indexPath = Paths.get("indexdir");
		Directory dir = FSDirectory.open(indexPath);
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(reader);

		Term term = new Term("content", "学*");
		Query wildcardQuery = new WildcardQuery(term);
		System.out.println("Query:" + wildcardQuery);

		FastVectorHighlighter highlighter = new FastVectorHighlighter();
		FieldQuery fieldQuery = highlighter.getFieldQuery(wildcardQuery);

		// 返回前10条
		TopDocs tds = searcher.search(wildcardQuery, 10);
		for (ScoreDoc sd : tds.scoreDocs) {
			// Explanation explanation = searcher.explain(query, sd.doc);
			// System.out.println("explain:" + explanation + "\n");
			Document doc = searcher.doc(sd.doc);
			System.out.println("DocID:" + sd.doc);
			System.out.println("id:" + doc.get("id"));
			System.out.println("title:" + doc.get("title"));
			System.out.println("文档评分:" + sd.score);

			// 高亮片段
			String[] frags = highlighter.getBestFragments(fieldQuery, reader, sd.doc, field, 100, 3);
			for (String frag : frags) {
				System.out.println("    " + frag);
			}
		}
		dir.close();
		reader.close();
	}
}
