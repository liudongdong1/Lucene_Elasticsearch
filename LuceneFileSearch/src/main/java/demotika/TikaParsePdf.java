package demotika;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class TikaParsePdf {
    public static void main(String[] args) throws IOException, SAXException, TikaException {
        // 文件路径
        String filepath = "基于用户移动行为相似性聚类的Markov位置预测_林树宽.pdf";
        // 新建File对象
        File pdfFile = new File(filepath);
        // 创建内容处理器对象
        BodyContentHandler handler = new BodyContentHandler();
        // 创建元数据对象
        Metadata metadata = new Metadata();

        // 读入文件
        FileInputStream inputStream = new FileInputStream(pdfFile);
        // InputStream inputStream=TikaInputStream.get(pdfFile);
        // 创建内容解析器对象
        ParseContext parseContext = new ParseContext();
        // 实例化PDFParser对象
        PDFParser parser = new PDFParser();
        // OOXMLParser parser = new OOXMLParser ();
        // TXTParser parser = new TXTParser();
        // HtmlParser parser = new HtmlParser();
        // 调用parse()方法解析文件
        parser.parse(inputStream, handler, metadata, parseContext);
        // 遍历元数据内容
        System.out.println("文件属性信息:");
        for (String name : metadata.names()) {
            System.out.println(name + ":" + metadata.get(name));
        }
        // 打印pdf文件中的内容
        System.out.println("pdf文件中的内容:");
        System.out.println(handler.toString());

    }
}
