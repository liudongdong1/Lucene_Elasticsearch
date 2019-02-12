package tup.lucene.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import tup.lucene.ik.IKAnalyzer6x;

public class CreateIndex {

    public static void main(String[] args) {

        // 创建2个News对象
        News news1 = new News();
        news1.setId(1);
        news1.setTitle("安倍晋三本周会晤特朗普 将强调日本对美国益处");
        news1.setContent("日本首相安倍晋三计划2月10日在华盛顿与美国总统特朗普举行会晤时提出加大日本在美国投资的设想");
        news1.setReply(672);
        News news2 = new News();
        news2.setId(2);
        news2.setTitle("北大迎4380名新生 农村学生700多人近年最多");
        news2.setContent("昨天，北京大学迎来4380名来自全国各地及数十个国家的本科新生。其中，农村学生共700余名，为近年最多...");
        news2.setReply(995);

        News news3 = new News();
        news3.setId(3);
        news3.setTitle("特朗普宣誓(Donald Trump)就任美国第45任总统");
        news3.setContent("当地时间1月20日，唐纳德·特朗普在美国国会宣誓就职，正式成为美国第45任总统。");
        news3.setReply(1872);
        // 开始时间

        Date start = new Date();
        System.out.println("**********开始创建索引**********");
        // 创建IK分词器
        Analyzer analyzer = new IKAnalyzer6x();
        /**Holds all the configuration that is used to create an IndexWriter. Once IndexWriter has been created with this object, changes to this object will
         *not affect the IndexWriter instance. For that, use LiveIndexWriterConfig that is returned from IndexWriter.getConfig().
         */
        IndexWriterConfig icw = new IndexWriterConfig(analyzer);
        //Specifies IndexWriterConfig.OpenMode of the index.
        icw.setOpenMode(OpenMode.CREATE);
        Directory dir = null;
        IndexWriter inWriter = null;
        // 索引目录
        Path indexPath = Paths.get("indexdir");

        try {
            if (!Files.isReadable(indexPath)) {
                System.out.println("Document directory '" + indexPath.toAbsolutePath()
                        + "' does not exist or is not readable, please check the path");
                System.exit(1);
            }
            dir = FSDirectory.open(indexPath);
            inWriter = new IndexWriter(dir, icw);
/**
 * Describes the properties of a field.
 * Sets the indexing options for the field:
 * Set to true to store this field
 * Set to true to tokenize this field's contents via the configured Analyzer.
 * */
            FieldType idType = new FieldType();
            idType.setIndexOptions(IndexOptions.DOCS);
            idType.setStored(true);

            FieldType titleType = new FieldType();
            titleType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            titleType.setStored(true);
            titleType.setTokenized(true);

            FieldType contentType = new FieldType();
            contentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            contentType.setStored(true);
            contentType.setTokenized(true);
            contentType.setStoreTermVectors(true);
            contentType.setStoreTermVectorPositions(true);
            contentType.setStoreTermVectorOffsets(true);
/**
 * ByteDocValuesField
 * Field that stores a per-document byte value for scoring, sorting or value retrieval.
 * CompressionTools
 * Simple utility class providing static methods to compress and decompress binary data for stored fields.
 * DateTools
 * Provides support for converting dates to strings and vice-versa.
 * DerefBytesDocValuesField
 * Field that stores a per-document BytesRef value.
 * Document
 * Documents are the unit of indexing and search.
 * DocumentStoredFieldVisitor
 * A StoredFieldVisitor that creates a Document containing all stored fields, or only specific requested fields provided to DocumentStoredFieldVisitor.DocumentStoredFieldVisitor(Set).
 * DoubleDocValuesField
 * Field that stores a per-document double value for scoring, sorting or value retrieval.
 * DoubleField
 * Field that indexes double values for efficient range filtering and sorting.
 * Field
 * Expert: directly create a field for a document.
 * FieldType
 * Describes the properties of a field.
 * FloatDocValuesField
 * Field that stores a per-document float value for scoring, sorting or value retrieval.
 * FloatField
 * Field that indexes float values for efficient range filtering and sorting.
 * IntDocValuesField
 * Field that stores a per-document int value for scoring, sorting or value retrieval.
 * IntField
 * Field that indexes int values for efficient range filtering and sorting.
 * LongDocValuesField
 * Field that stores a per-document long value for scoring, sorting or value retrieval.
 * LongField
 * Field that indexes long values for efficient range filtering and sorting.
 * PackedLongDocValuesField
 * Field that stores a per-document long value for scoring, sorting or value retrieval.
 * ShortDocValuesField
 * Field that stores a per-document short value for scoring, sorting or value retrieval.
 * SortedBytesDocValuesField
 * Field that stores a per-document BytesRef value, indexed for sorting.
 * StoredField
 * A field whose value is stored so that IndexSearcher.doc(int) and IndexReader.document(int, org.apache.lucene.index.StoredFieldVisitor) will return the field and its value.
 * StraightBytesDocValuesField
 * Field that stores a per-document BytesRef value.
 * StringField
 * A field that is indexed but not tokenized: the entire String value is indexed as a single token.
 * TextField
 * A field that is indexed and tokenized, without term vectors.
 * */
            Document doc1 = new Document();
            doc1.add(new Field("id", String.valueOf(news1.getId()), idType));
            doc1.add(new Field("title", news1.getTitle(), titleType));
            doc1.add(new Field("content", news1.getContent(), contentType));
            doc1.add(new IntPoint("reply", news1.getReply()));
            doc1.add(new StoredField("reply_display", news1.getReply()));

            Document doc2 = new Document();
            doc2.add(new Field("id", String.valueOf(news2.getId()), idType));
            doc2.add(new Field("title", news2.getTitle(), titleType));
            doc2.add(new Field("content", news2.getContent(), contentType));
            doc2.add(new IntPoint("reply", news2.getReply()));
            doc2.add(new StoredField("reply_display", news2.getReply()));

            Document doc3 = new Document();
            doc3.add(new Field("id", String.valueOf(news3.getId()), idType));
            doc3.add(new Field("title", news3.getTitle(), titleType));
            doc3.add(new Field("content", news3.getContent(), contentType));
            doc3.add(new IntPoint("reply", news3.getReply()));
            doc3.add(new StoredField("reply_display", news3.getReply()));

            inWriter.addDocument(doc1);
            inWriter.addDocument(doc2);
            inWriter.addDocument(doc3);

            inWriter.commit();

            inWriter.close();
            dir.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Date end = new Date();
        System.out.println("索引文档用时:" + (end.getTime() - start.getTime()) + " milliseconds");
        System.out.println("**********索引创建完成**********");
    }
}
