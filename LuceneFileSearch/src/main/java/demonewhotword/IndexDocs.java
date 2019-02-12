package demonewhotword;

import java.io.*;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import sun.rmi.runtime.Log;
import tup.lucene.ik.IKAnalyzer6x;

/**
 * @author ldd
 * @description 读取小女花不弃(完整下载).txt，并建立索引
 * 解决了txt文件读取乱码问题
 * */
public class IndexDocs {

    public static void main(String[] args) throws IOException {

       // String text1 = readTxt("小女花不弃(完整下载).txt");
         File newsfile = new File("小女花不弃(完整下载).txt");
        String text1 = textToString(newsfile);
        //System.out.println("全文读取如下：/n"+text1);
        // Analyzer smcAnalyzer = new SmartChineseAnalyzer(true);
        Analyzer smcAnalyzer = new IKAnalyzer6x(true);
        //Analyzer smcAnalyzer=new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(smcAnalyzer);
        indexWriterConfig.setOpenMode(OpenMode.CREATE);
        // 索引的存储路径
        Directory directory = null;
        // 索引的增删改由indexWriter创建
        IndexWriter indexWriter = null;
        directory = FSDirectory.open(Paths.get("indexdir"));
        indexWriter = new IndexWriter(directory, indexWriterConfig);

        // 新建FieldType,用于指定字段索引时的信息
        FieldType type = new FieldType();
        // 索引时保存文档、词项频率、位置信息、偏移信息
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        type.setStored(true);// 原始字符串全部被保存在索引中
        type.setStoreTermVectors(true);// 存储词项量
        type.setTokenized(true);// 词条化
        Document doc1 = new Document();
        Field field1 = new Field("content", text1, type);
        doc1.add(field1);
       // System.out.println(doc1.toString());
        indexWriter.addDocument(doc1);
        indexWriter.close();
        directory.close();
    }

    public static String textToString(File file) {
        Logger logger=Logger.getLogger("text");
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"gb2312"));// 构造一个BufferedReader类来读取文件
            String str = null;
            while ((str = br.readLine()) != null) {// 使用readLine方法，一次读一行
                logger.info(str);
                result.append(System.lineSeparator() + str);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 解析普通文本文件  流式文件 如txt
     * @param path
     * @return
     */
    @SuppressWarnings("unused")
    public static String readTxt(String path){
        StringBuilder content = new StringBuilder("");
        try {
            String code = resolveCode(path);
            File file = new File(path);
            InputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is, code);
            BufferedReader br = new BufferedReader(isr);
            String str = "";
            while (null != (str = br.readLine())) {
                content.append(str);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("读取文件:" + path + "失败!");
        }
        return content.toString();
    }



    public static String resolveCode(String path) throws Exception {
//      [-76, -85, -71]  ANSI
//      [-2, -1, 79] unicode big endian
//      [-1, -2, 32]  unicode
//      [-17, -69, -65] UTF-8
        InputStream inputStream = new FileInputStream(path);
        byte[] head = new byte[3];
        inputStream.read(head);
        String code = "gb2312";  //或GBK
        if (head[0] == -1 && head[1] == -2 )
            code = "UTF-16";
        else if (head[0] == -2 && head[1] == -1 )
            code = "Unicode";
        else if(head[0]==-17 && head[1]==-69 && head[2] ==-65)
            code = "UTF-8";

        inputStream.close();
        System.out.println(code);
        return code;
    }


}

