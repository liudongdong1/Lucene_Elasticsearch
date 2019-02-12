package tup.lucene.analyzer;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import tup.lucene.ik.IKAnalyzer6x;

public class IkVSSmartcn {

	private static String str1 = "安倍晋三本周会晤特朗普 将强调日本对美国益处";
	private static String str2 = "IKAnalyzer是一个开源的,基于java语言开发的轻量级的中文分词工具包。";
	private static String str3 = "厉害了我的哥!中国智造研发出了抵抗北京雾霾的的方法!";

	public static void main(String[] args) throws IOException {
		Analyzer analyzer = null;
		System.out.println("句子1:"+str1);
		System.out.println("SmartChineseAnalyzer分词结果:");
		
		analyzer = new SmartChineseAnalyzer();
		printAnalyzer(analyzer, str1);
		System.out.println("IKAnalyzer分词结果:");
		analyzer = new IKAnalyzer6x(true);
		printAnalyzer(analyzer, str1);
		
		
		System.out.println("-------------------------------------------");
		System.out.println("句子2:"+str2);
		System.out.println("SmartChineseAnalyzer分词结果:");
		analyzer = new SmartChineseAnalyzer();
		printAnalyzer(analyzer, str2);
		System.out.println("IKAnalyzer分词结果:");
		analyzer = new IKAnalyzer6x(true);
		printAnalyzer(analyzer, str2);
	
		
		System.out.println("-------------------------------------------");
		System.out.println("句子3:"+str3);
		System.out.println("SmartChineseAnalyzer分词结果:");
		analyzer = new SmartChineseAnalyzer();
		printAnalyzer(analyzer, str3);
		System.out.println("IKAnalyzer分词结果:");
		analyzer = new IKAnalyzer6x(true);
		printAnalyzer(analyzer, str3);
		analyzer.close();
	}

	public static void printAnalyzer(Analyzer analyzer, String str) throws IOException {
		StringReader reader = new StringReader(str);
		TokenStream toStream = analyzer.tokenStream(str, reader);
		toStream.reset();// 清空流
		CharTermAttribute teAttribute = toStream.getAttribute(CharTermAttribute.class);
		while (toStream.incrementToken()) {
			System.out.print(teAttribute.toString() + "|");
		}
		System.out.println();
	}
}
