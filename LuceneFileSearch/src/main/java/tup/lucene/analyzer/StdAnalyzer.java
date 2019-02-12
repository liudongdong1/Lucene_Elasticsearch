package tup.lucene.analyzer;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import tup.lucene.ik.IKAnalyzer6x;

public class StdAnalyzer {
	private static String strCh = "中华人民共和国简称中国, 是一个有13亿人口的国家";
	private static String strEn = "Dogs can not achieve a place,eyes can reach; ";
	public static void main(String[] args) throws IOException {
		System.out.println("StandardAnalyzer对中文分词:");
		stdAnalyzer(strCh);
		System.out.println("StandardAnalyzer对英文分词:");
		stdAnalyzer(strEn);
		
	}
	public static void stdAnalyzer(String str) throws IOException{
		/**
		 * represents a policy for extracting index terms from text.
		 * 具体方法有：KeywordAnalyzer, PerFieldAnalyzerWrapper, SimpleAnalyzer, StandardAnalyzer, StopAnalyzer, WhitespaceAnalyzer
		 * An Analyzer builds TokenStreams, which analyze text. It thus represents a policy for extracting index terms from text.
		 * */
		Analyzer analyzer = null;
		analyzer = new StandardAnalyzer();
		//A character stream whose source is a string.
		StringReader reader = new StringReader(str);
		//Creates a TokenStream which tokenizes all the text in the provided Reader. Must be able to handle null field name for backward compatibility.
		TokenStream toStream = analyzer.tokenStream(str, reader);
		//This method is called by a consumer before it begins consumption using incrementToken().
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
