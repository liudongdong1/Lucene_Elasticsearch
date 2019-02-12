package tup.lucene.ik;

import java.io.IOException;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
/**
 * Tokenizers break up text into individual Objects. These objects may be Strings, Words, or other Objects. A Tokenizer extends the Iterator interface, but provides a lookahead operation peek(). An implementation of this interface is expected to have a constructor that takes a single argument, a Reader.
 *CharTermAttribute              表示token本身的内容
 * PositionIncrementAttribute  表示当前token相对于前一个token的相对位置，也就是相隔的词语数量（例如“text for attribute”，
 *                                           text和attribute之间的getPositionIncrement为2），如果两者之间没有停用词，那么该值被置为默认值1
 * OffsetAttribute   表示token的首字母和尾字母在原文本中的位置
 * TypeAttribute    表示token的词汇类型信息，默认值为word，
 *    其它值有<ALPHANUM> <APOSTROPHE> <ACRONYM> <COMPANY> <EMAIL> <HOST> <NUM> <CJ> <ACRONYM_DEP>
 * FlagsAttribute 与TypeAttribute类似，假设你需要给token添加额外的信息，而且希望该信息可以通过分析链，那么就可以通过flags去传递
 * PayloadAttribute   在每个索引位置都存储了payload（关键信息），当使用基于Payload的查询时，该信息在评分中非常有用
 * */
public class IKTokenizer6x extends Tokenizer {
	// IK分词器实现
	private IKSegmenter _IKImplement;
	// 词元文本属性
	private final CharTermAttribute termAtt;
	// 词元位移属性
	private final OffsetAttribute offsetAtt;
	// 词元分类属性（该属性分类参考org.wltea.analyzer.core.Lexeme中的分类常量）
	private final TypeAttribute typeAtt;
	// 记录最后一个词元的结束位置
	private int endPosition;

	// Lucene 6.x Tokenizer适配器类构造函数;实现最新的Tokenizer接口
	public IKTokenizer6x(boolean useSmart) {
		super();
		offsetAtt = addAttribute(OffsetAttribute.class);
		termAtt = addAttribute(CharTermAttribute.class);
		typeAtt = addAttribute(TypeAttribute.class);
		//useSmart - 为true，使用智能分词策略 非智能分词：细粒度输出所有可能的切分结果 智能分词： 合并数词和量词，对分词结果进行歧义判断
		_IKImplement = new IKSegmenter(input, useSmart);
	}

	@Override
	public boolean incrementToken() throws IOException {
		// 清除所有的词元属性
		clearAttributes();
		//.next获取下一个词元
		Lexeme nextLexeme = _IKImplement.next();
		if (nextLexeme != null) {
			// 将Lexeme转成Attributes
			// 设置词元文本
			termAtt.append(nextLexeme.getLexemeText());
			// 设置词元长度
			termAtt.setLength(nextLexeme.getLength());
			// 设置词元位移
			offsetAtt.setOffset(nextLexeme.getBeginPosition(), nextLexeme.getEndPosition());
			// 记录分词的最后位置
			endPosition = nextLexeme.getEndPosition();
			// 记录词元分类
			typeAtt.setType(nextLexeme.getLexemeText());
			// 返会true告知还有下个词元
			return true;
		}
		// 返会false告知词元输出完毕
		return false;
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		_IKImplement.reset(input);
	}

	@Override
	public final void end() {
		int finalOffset = correctOffset(this.endPosition);
		offsetAtt.setOffset(finalOffset, finalOffset);
	}
}