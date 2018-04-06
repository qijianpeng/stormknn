package validate;


import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.hdfs.common.HdfsUtils;
import org.khelekore.prtree.*;
import storm.hdfs.spout.ParseException;
import storm.hdfs.spout.TextFileReader;
import util.Configuration;
import util.args.MainArgsHandler;
import util.router.Router;
import vo.Point;
import vo.Point2DConverter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * 异常检测串行算法，支持Terminal输入参数。
 * 参数defaults：
 *  --index.branch.factor=40 --K=20 --query.num=-1
 *
 * Created by qijianpeng on 28/08/2017.
 * mail: jianpengqi@126.com
 */
public class StreamKNNWithGrid implements Serializable {
    private static final long serialVersionUID = 1L;
    static int QUERY_NUM = -1;

    public static void main(String[] args) throws IOException, ParseException {
       // OutlierAlgorithm oa = new DBOutlierAlgorithm();


        init(args);
        int query_num = QUERY_NUM;
        PRTree<Point> tree;
        Point2DConverter converter;
        DistanceCalculator<Point> dc;
        NodeFilter<Point> acceptAll;

        converter = new Point2DConverter();
        dc = new Point();
        acceptAll = new AcceptAll<>();

        Configuration.HDFS_URI = "hdfs://centos1:9000";
        Path sourceDirPath = new Path(Configuration.HDFS_URI + Configuration.HDFS_SOURCE_DIR);
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        conf.set("fs.hdfs.impl",
                org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
        );
        conf.set("fs.file.impl",
                org.apache.hadoop.fs.LocalFileSystem.class.getName()
        );
        FileSystem hdfs = FileSystem.get(URI.create(Configuration.HDFS_URI), conf);
        Path file = HdfsUtils.listFilesByModificationTime(hdfs, sourceDirPath, 0).get(0);
        TextFileReader textFileReader = new TextFileReader(hdfs, file, null);
       // int slideInterval = Configuration.SLIDE_INTERVAL;
        Set<Point> slides = new HashSet<>();
        //   WindowMap<Long, Node> window = new WindowMap<>(Configuration.SLIDES);
        Long slideTime = 0L;
        List<Object> singletonList = null;
        long startTime = System.currentTimeMillis();
        List<List<Object>> datasources = new ArrayList<>();
        Map<String, Object> config = new HashMap<>();

        config.put(Configuration.NAME_BOUNDARY_LENGTH_SCALE, Configuration.BOUNDARY_LENGTH_SCALE);
        config.put(Configuration.NAME_INDEX_HIGHT, Configuration.INDEX_HIGHT);

        Router _router = new Router(config);
        _router.init();

        while (null != (singletonList = textFileReader.nextByDefaultFields())){
            datasources.add(singletonList);
        }
        long queries_counter = 0;
        int slide_counter = 0;
        Iterator<List<Object>> iter = datasources.iterator();
        while (iter.hasNext()) {
            singletonList = iter.next();
            for (String line : (List<String>) singletonList.toArray()[0]) {
                Point point = Point.newInstance(line.toString());
                if (query_num > 0) {
                    point.setQuery(true);
                    query_num--;
                }
                if (query_num == -1) point.setQuery(true);

                slides.add(point);
            }

            HashMap<Integer, Set<Point>> pointsRouting = new HashMap<>();
            for (Point point : slides) {
                Map<Integer, Character> routs = _router.getIndexes(point);
                for (Map.Entry<Integer, Character> r : routs.entrySet()) {
                    Integer route = r.getKey();
                    boolean isBoundary = r.getValue().equals('T') ? true : false;
                    if (null == pointsRouting.get(route)) {
                        pointsRouting.put(route, new HashSet<>());
                    }

                    Point p = new Point(point);
                    p.setBoundary(isBoundary);
                    if (isBoundary) {
                        p.setQuery(false);
                    }
                    pointsRouting.get(route).add(p);
                }
            }
            //2.1 emitting tuples
            Iterator<Map.Entry<Integer, Set<Point>>> iterRouter = pointsRouting.entrySet().iterator();
            slideTime++;
            while (iterRouter.hasNext()) {
                Map.Entry<Integer, Set<Point>> message = iterRouter.next();
                Set<Point> points = message.getValue();
                tree = new PRTree<Point>(converter, Configuration.BRANCH_FACTOR);
                tree.load(points);
                for (Point point : points) {
                    if (point.isBoundary() || !point.isQuery())continue;
                    queries_counter++;
                    List<DistanceResult<Point>> res = tree.nearestNeighbour(dc, acceptAll, Configuration.K + 1, point);

                }
            }

            System.out.println("Slide Time: " + slideTime + ", Slide size:" + slides.size());
                    // if (slideTime == Configuration.DATA_ENDTIME)break;
            slides.clear();
            query_num = QUERY_NUM;
            slide_counter++;
        }

        long endTime = System.currentTimeMillis();
        long total_time = endTime - startTime;
        long queries_per_slide = queries_counter / slide_counter;
        long ms_per_slide = total_time / slide_counter;
        System.out.println();
        System.out.println("Total_time(ms)\tAverage_time_per_slide(ms)\tqueries_per_slide(points)\tslides_counter");
        System.out.println(total_time + "\t\t" + ms_per_slide + "\t\t" + queries_per_slide+"\t\t"+slide_counter);
    }


    /**
     * 序列化器注册
     */
    static void init(String[] args){


        if (null != args && args.length > 0) {

            MainArgsHandler mah = MainArgsHandler.getHandler();

            mah.permitVariable(Configuration.NAME_BRANCH_FACTOR, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_K, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_QUERY, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_BOUNDARY_LENGTH_SCALE, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_INDEX_HIGHT, MainArgsHandler.ZERO_OR_ONE);
            mah.processMainArgs(args);



            if (mah.foundVariable(Configuration.NAME_BOUNDARY_LENGTH_SCALE)){
                Configuration.BOUNDARY_LENGTH_SCALE = Double.parseDouble(
                        mah.getValuesFromVariable(Configuration.NAME_BOUNDARY_LENGTH_SCALE).get(0).toString()
                );
            }
            if (mah.foundVariable(Configuration.NAME_INDEX_HIGHT)){
                Configuration.INDEX_HIGHT = Integer.valueOf(mah.getValuesFromVariable(Configuration.NAME_INDEX_HIGHT).get(0).toString());
            }
            if (mah.foundVariable(Configuration.NAME_BRANCH_FACTOR)){
                Configuration.BRANCH_FACTOR =  Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_BRANCH_FACTOR).get(0));
            }


            if (mah.foundVariable(Configuration.NAME_K)){
                Configuration.K =  Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_K).get(0));
            }


            if (mah.foundVariable(Configuration.NAME_QUERY)){
                QUERY_NUM = Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_QUERY).get(0).toString());
            }else{
                QUERY_NUM = -1;//all
            }
        }
    }


}
