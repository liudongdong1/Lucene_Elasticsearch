package demotika;


import java.io.File;
import java.io.IOException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

public class TikaExtraction {
    public static void main(String[] args) throws IOException, TikaException {
        Tika tika = new Tika();
        // 新建存放各种文件的files文件夹
        File fileDir = new File("files");
        // 如果文件夹路径错误，退出程序
        if (!fileDir.exists()) {
            System.out.println("文件夹不存在, 请检查!");
            System.exit(0);
        }
        // 获取文件夹下的所有文件，存放在File数组中
        File[] fileArr = fileDir.listFiles();
        String filecontent;
        for (File f : fileArr) {
            filecontent = tika.parseToString(f);        // 自动解析
            System.out.println("Extracted Content: " + filecontent);
        }
    }
}
