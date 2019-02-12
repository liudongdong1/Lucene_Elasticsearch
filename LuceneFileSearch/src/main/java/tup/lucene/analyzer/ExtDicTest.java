package tup.lucene.analyzer;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import tup.lucene.ik.IKAnalyzer6x;

public class ExtDicTest {

	private static String str = "厉害了我的哥!中国环保部门发布了治理北京雾霾的的方法!";
	public static void main(String[] args) throws IOException {
		Analyzer analyzer = new IKAnalyzer6x(true);
		StringReader reader = new StringReader(str);
		TokenStream toStream = analyzer.tokenStream(str, reader);
		toStream.reset();
		CharTermAttribute teAttribute = toStream.getAttribute(CharTermAttribute.class);
		System.out.println("分词结果:");
		while (toStream.incrementToken()) {
			System.out.print(teAttribute.toString() + "|");
		}
		System.out.println("\n");
		analyzer.close();
	}
}
