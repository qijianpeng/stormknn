package util;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by qijianpeng on 19/02/2017.
 */
public class Configuration {
    /**
     * @see <a href="{@docRoot}/configuration.xml">resources/configureation.xml</a>
     */
    public static Map<String, Object> cfg = null;
    private static final InputStream xml =
            Configuration.class.getClass().getResourceAsStream("/configuration.xml");
    static {
        try {
            cfg = XMLUtil.readConfig(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * data dimensions.
     *
     */

    public static  int DIMENSIONS = (Integer)(Configuration.cfg.get("data.dimensions"));
    public static final String DATA_SEPARATOR = (String)(Configuration.cfg.get("data.separator"));
    public static final Integer DATA_TIMEFIELDS_INDEX = (Integer)(Configuration.cfg.get("data.timefields.index"));

    public static final double RADIUS = (Double)Configuration.cfg.get("radius");
    public static Integer K = (Integer)  Configuration.cfg.get("K");

    public static final double MIN_CELL_WIDTH = RADIUS/(2*Math.sqrt(DIMENSIONS));
    /**
     * 索引树叶节点包含的最小Cell数量. 最小Cell的长度是由Radius计算而来.
     */
    public static final int CELL_LENGTH = (Integer)Configuration.cfg.get("index.grid.cell.length");

    public static  int INDEX_HIGHT = (Integer) Configuration.cfg.get("index.height");

    public static  double BOUNDARY_LENGTH_SCALE = (Double) Configuration.cfg.get("index.boundary.length.scale");

    public static  int BRANCH_FACTOR = (Integer)Configuration.cfg.get("index.branch.factor");

    public static  int KNN_BOLT_NUM = (Integer)Configuration.cfg.get("topology.knnBolt.num");

    public static long DATA_ENDTIME = (Long) Configuration.cfg.get("data.endtime");


    public static final String NAME_QUERY = "query.num";

   // public static final int BOLTS_NUM = (Integer)Configuration.cfg.get("storm.bolts.num");
    public static final int SLIDE_INTERVAL = (Integer) Configuration.cfg.get("stream.slide.interval");
    /**
     * Window内所有的时间戳数目, 注意, 不是Slide数目
     */
    public static final int WINDOW_SIZE = (Integer)Configuration.cfg.get("stream.window");

    /**
     * Window中包含的Slide数目.
     */
    public static final int SLIDES = WINDOW_SIZE / SLIDE_INTERVAL;
    public static final double IMBALANCE_DEGREE_HEIGHER =
                                            (Double)Configuration.cfg.get("router.imbalanceDegree");
    public static long LATEST_TIME = 0L;
    public static long LATEST_SLIDE_TIME = 0L;
    public static final double IMBALANCE_DEGREE_LOWER =
                                        (Double)Configuration.cfg.get("router.imbalanceDegree.lower");
    private static Double MIN_X =  (Double)Configuration.cfg.get("data.minX");
    private static Double MAX_X =  (Double)Configuration.cfg.get("data.maxX");
    private static Double MIN_Y =  (Double)Configuration.cfg.get("data.minY");
    private static Double MAX_Y =  (Double)Configuration.cfg.get("data.maxY");
    private static Double MIN_Z = null;
    private static Double MAX_Z = null;
    public static double[][] DEFAULT_AREA = null;
    static  {
        if (DIMENSIONS == 3){
            MIN_Z = (Double)Configuration.cfg.get("data.minZ");
            MAX_Z = (Double)Configuration.cfg.get("data.maxZ");
            DEFAULT_AREA = new double[][]{new double[]{MIN_X, MAX_X}, new double[]{MIN_Y, MAX_Y},
                                            new double[]{MIN_Z, MAX_Z}};
          /*  GRID_DIMENSIONS = (int) Math.floor(Arrays.asList((MAX_X-MIN_X), (MAX_Y - MIN_Y), (MAX_Z - MIN_Z))
                    .stream().min((x,y)->x.compareTo(y)).get()/MIN_CELL_WIDTH);*/
        }else if (DIMENSIONS == 2){
            DEFAULT_AREA = new double[][]{new double[]{MIN_X, MAX_X}, new double[]{MIN_Y, MAX_Y}};
           /* GRID_DIMENSIONS = (int) Math.floor(Arrays.asList((MAX_X-MIN_X), (MAX_Y - MIN_Y))
                   .stream().min((x,y)->x.compareTo(y)).get()/MIN_CELL_WIDTH);*/
        }

    }

    /**
     * HDFS settings
     */
    public static String HDFS_URI =  Configuration.cfg.get("hdfs.uri").toString();
    public static String HDFS_ARCHIVE_DIR =  Configuration.cfg.get("hdfs.archiveDir").toString();
    public static String HDFS_SOURCE_DIR =  Configuration.cfg.get("hdfs.sourceDir").toString();
    public static String HDFS_BADFILES_DIR =  Configuration.cfg.get("hdfs.badFilesDir").toString();


    public static Integer RESULTS_BOLTS_NUM = 1;

    /**
     * NAMES
     */
    public static final String NAME_INDEX_HIGHT = "index.height";
    public static final String NAME_BOUNDARY_LENGTH_SCALE = "index.boundary.length.scale";
    public static final String NAME_K = "K";
    public static final String NAME_KNN_BOLT_NUM = "topology.knnBolt.num";
    public static final String NAME_BRANCH_FACTOR = "index.branch.factor";

    public static final String NAME_DATA_END_TIME = "data.endtime";
    public static boolean BALANCE =  (boolean) Configuration.cfg.get("router.balance");
    public static void main(String[] args) {
        System.out.println(cfg);
    }
}
