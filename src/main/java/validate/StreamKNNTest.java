package validate;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.hdfs.common.HdfsUtils;
import org.khelekore.prtree.*;
import storm.hdfs.spout.ParseException;
import storm.hdfs.spout.TextFileReader;
import util.Configuration;
import util.router.Router;
import vo.Point;
import vo.Point2DConverter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 异常检测单机处理
 * Created by qijianpeng on 28/08/2017.
 * mail: jianpengqi@126.com
 */
public class StreamKNNTest implements Serializable {
    private static final long serialVersionUID = 1L;


    public static void main(String[] args) throws IOException, ParseException {
       // OutlierAlgorithm oa = new DBOutlierAlgorithm();

        Router _router = new Router();
        _router.init();

        final int QUERY_SCALE = 10;
        final  int QUERY_NUM = 1000 * QUERY_SCALE;
        int query_num = QUERY_NUM;

        int DISTANCE_SCALE = 1;



        double minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        double maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        PRTree<Point> tree;
        Point2DConverter converter;
        DistanceCalculator<Point> dc;
        NodeFilter<Point> acceptAll;

        converter = new Point2DConverter();
        dc = new Point();
        acceptAll = new AcceptAll<>();



        Configuration.HDFS_URI="hdfs://localhost:9000";
        Path sourceDirPath = new Path(Configuration.HDFS_URI+Configuration.HDFS_SOURCE_DIR);
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        FileSystem hdfs = FileSystem.get(URI.create(Configuration.HDFS_URI), conf);
        Path file = HdfsUtils.listFilesByModificationTime(hdfs, sourceDirPath, 0).get(0);
        TextFileReader textFileReader = new TextFileReader(hdfs, file ,null);
        File localDestFile = new File("/Users/qijianpeng/Downloads/StreamKNN/Serial_results_beijingCircle2_20180302_30t_"+QUERY_SCALE+"k.txt");
        int slideInterval = Configuration.SLIDE_INTERVAL;
        Set<Point> slides = new HashSet<>();
     //   WindowMap<Long, Node> window = new WindowMap<>(Configuration.SLIDES);
        Long slideTime = 0L;
        Set<Long> timesets = new HashSet<>();
        List<Object> singletonList = null;
        int count = 0;
        long startTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        Set<String> countsSet = new HashSet<>();
        while (null != (singletonList =  textFileReader.nextByDefaultFields())) {

            if(slideInterval > 0){
                slideInterval--;
                for (String line : (List<String>)singletonList.toArray()[0]){
                    Point point = Point.newInstance(line.toString());
                    if (query_num > 0){
                        point.setQuery(true);
                        query_num --;
                    }
                    if (query_num == -1)point.setQuery(true);
                    slides.add(point);
                    point.setX(point.getX()*DISTANCE_SCALE);
                    point.setY(point.getY()*DISTANCE_SCALE);
                }
                if (slideInterval == 0) {

                    slideTime++;
                   // tree = new PRTree<Point> (converter, Configuration.BRANCH_FACTOR);
                    //tree.load(slides);
                    int counter = 0;
                    for (Point point : slides) {
                        countsSet.add(point.getId());
                        if (minX > point.getX())minX = point.getX();
                        if (minY > point.getY())minY = point.getY();

                        if (maxX < point.getX())maxX = point.getX();
                        if (maxY < point.getY())maxY = point.getY();

                        Map<Integer, Character> routs = _router.getIndexes(point);
                        // System.out.println("Paths:"+routs.size());
                        for (Map.Entry<Integer, Character> r : routs.entrySet()){
                            Integer route = r.getKey();
                            boolean isBoundary = r.getValue().equals('T') ? true : false;

                            Point p = new Point(point);
                            p.setBoundary(isBoundary);
                            if (isBoundary) {
                                p.setQuery(false);

                            }
                            if (p.isQuery())counter++;
                        }
                    }
                    System.out.println("TOTAL QUERY POINTS:"+counter +"\tTOTAL_POINTS:"+slides.size());

                    slides.clear();
                    slideInterval = Configuration.SLIDE_INTERVAL;
                }
            }

            query_num = QUERY_NUM;
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time: "+ (endTime - startTime));
        System.out.println("X: "+minX +"\t"+maxX);
        System.out.println("Y: "+minY +"\t"+maxY);
        System.out.println("Total points: "+ countsSet.size());
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
