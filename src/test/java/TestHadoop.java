import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.hdfs.spout.HdfsSpout;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by qijianpeng on 14/06/2017.
 */
public class TestHadoop {
    public static void main(String[] args) throws IOException {


        FileSystem fs = FileSystem.get(URI.create("hdfs://localhost:9000"), new Configuration());
        String hdfspath = "/Users/qijianpeng/Downloads/10g_50obj_50t_sorted.txt";
        Path src = new Path(hdfspath);

        Path deletePath = new Path("/storm/");
        if (fs.exists(deletePath))fs.delete(deletePath, true);
        Path dest = new Path("/storm/data");
        boolean mkdirs = fs.mkdirs(dest);
        if (mkdirs){
            fs.copyFromLocalFile(src, dest);
        }else {
            System.out.println("upload failed.");
        }

        fs.close();
    }
}
