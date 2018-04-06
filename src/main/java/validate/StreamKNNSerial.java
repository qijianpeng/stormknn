package validate;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.hdfs.common.HdfsUtils;
import org.apache.storm.tuple.Values;
import org.khelekore.prtree.*;
import storm.hdfs.spout.ParseException;
import storm.hdfs.spout.TextFileReader;
import util.Configuration;
import vo.Point;
import vo.Point2DConverter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 异常检测单机处理
 * Created by qijianpeng on 28/08/2017.
 * mail: jianpengqi@126.com
 */
public class StreamKNNSerial implements Serializable {
    private static final long serialVersionUID = 1L;


    public static void main(String[] args) throws IOException, ParseException {
       // OutlierAlgorithm oa = new DBOutlierAlgorithm();
        for (int QUERY_SCALE = 20; QUERY_SCALE <= 20; QUERY_SCALE+=10) {
           // if (QUERY_SCALE != 40 && QUERY_SCALE != 60)continue;
            System.out.println(QUERY_SCALE);
            final int QUERY_NUM = -1;//1000 * QUERY_SCALE;
            int query_num = QUERY_NUM;
            Configuration.K = QUERY_SCALE;
            PRTree<Point> tree;
            Point2DConverter converter;
            DistanceCalculator<Point> dc;
            NodeFilter<Point> acceptAll;

            converter = new Point2DConverter();
            dc = new Point();
            acceptAll = new AcceptAll<>();



            Configuration.HDFS_URI = "hdfs://localhost:9000";
            Path sourceDirPath = new Path(Configuration.HDFS_URI + Configuration.HDFS_SOURCE_DIR);
            org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
            FileSystem hdfs = FileSystem.get(URI.create(Configuration.HDFS_URI), conf);
            Path file = HdfsUtils.listFilesByModificationTime(hdfs, sourceDirPath, 0).get(0);
            TextFileReader textFileReader = new TextFileReader(hdfs, file, null);
            File localDestFile = new File("/Users/qijianpeng/Downloads/StreamKNN/Serial_results_beijingCircle2_20180302_40t_top" + QUERY_SCALE + ".txt");
            int slideInterval = Configuration.SLIDE_INTERVAL;
            Set<Point> slides = new HashSet<>();
            //   WindowMap<Long, Node> window = new WindowMap<>(Configuration.SLIDES);
            Long slideTime = 0L;
            Set<Long> timesets = new HashSet<>();
            List<Object> singletonList = null;
            int count = 0;
            long startTime = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            while (null != (singletonList = textFileReader.nextByDefaultFields())) {

                if (slideInterval > 0) {
                    slideInterval--;
                    for (String line : (List<String>) singletonList.toArray()[0]) {
                        Point point = Point.newInstance(line.toString());
                        if (query_num > 0) {
                            point.setQuery(true);
                            query_num--;
                        }
                        if (query_num == -1) point.setQuery(true);
                        slides.add(point);
                    }
                    if (slideInterval == 0) {
                        slideTime++;
                        tree = new PRTree<Point>(converter, Configuration.BRANCH_FACTOR);
                        tree.load(slides);
                        for (Point point : slides) {
                            if (!point.isQuery()) continue;
                            List<DistanceResult<Point>> res = tree.nearestNeighbour(dc, acceptAll, Configuration.K + 1, point);
                            List<Point> topK = res.stream().map(pointDistanceResult ->
                                    pointDistanceResult.get()).collect(Collectors.toList());
                            List<Double> topKDistance = res.stream().map(pointDistanceResult ->
                                    pointDistanceResult.getDistance()).collect(Collectors.toList());

                            sb.append(slideTime + "\t");
                            sb.append(point.toString() + "\t");
                            for (int i = 0; i < topK.size(); i++) {
                                sb.append(topK.get(i).toString() /*+ topKDistance.get(i)*/ + "\t");
                            }
                            sb.append("\n");
                        }
                        System.out.println("Slide Time: " + slideTime + ", Slide size:" + slides.size());
                        writeStrsToLocal(sb.toString() + "\n", localDestFile);
                        sb.setLength(0);
                        // if (slideTime == Configuration.DATA_ENDTIME)break;
                        slides.clear();
                        slideInterval = Configuration.SLIDE_INTERVAL;
                    }
                }

                query_num = QUERY_NUM;
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Time: " + (endTime - startTime));
        }
    }
    public static void writeOutliersToHDFS(Long slidestamp, Set<Point> outliers, Path destFile, FileSystem fs, FSDataOutputStream os) throws IOException {
        // System.out.println(sb.toString());
        os.write(outliersToString(slidestamp, outliers).getBytes());
    }

    public static void writeOutliersToLocal(Long slidestamp, Set<Point> outliers, File file) throws IOException {
        FileUtils.writeStringToFile(file, outliersToString(slidestamp, outliers), "utf-8", true);
    }
    public static void writeStrsToLocal(String str, File file) throws IOException {
        FileUtils.writeStringToFile(file, str, "utf-8", true);
    }

    public static String outliersToString(Long slidestamp, Set<Point> outliers){
        String start = (slidestamp - Configuration.SLIDES > 0? slidestamp - Configuration.SLIDES: 0) +"";
        StringBuilder sb = new StringBuilder();
        for (Point point: outliers){
            sb.append(start+",");
            sb.append(slidestamp+",");
            sb.append(point.getId() +",");
            sb.append( point.getTimestamp()/Configuration.SLIDE_INTERVAL) ;
            sb.append("\n");
        }
        //判断文件是否存在, 不存在则创建, 存在则追加数据
        return sb.toString();
    }

}
