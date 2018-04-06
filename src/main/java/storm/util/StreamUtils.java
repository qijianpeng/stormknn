package storm.util;

/**
 * Created by qijianpeng on 27/06/2017.
 */
public class StreamUtils {



    public static final String SPOUT_HDFS = "HDFS_SPOUT";
    public static final String BOLT_ROUTER = "BOLT_ROUTER";
    public static final String BOLT_KNN = "BOLT_KNN";

    public static final String STREAM_POINTS = "STREAM_POINT";
    public static final String STREAM_RESULT = "STREAM_RESULT";

    public static final String STREAM_TIME_COUNTER = "STREAM_TIME_COUNTER";

    public static final String STREAM_ROUTER_TO_KNN = "STREAM_ROUTER_TO_KNN";

    public static class LATE_TUPLES{
        public static String lateTuplesFromOutlierAggregateBoltStream = "LateTuplesFromOutlierAggregateBolt";
        public static String lateTuplesFromOutlierBoundaryBolt = "LateTuplesFromOutlierBoundaryBolt";
        public static String lateTuplesFromOutlierBoundaryBoltAggerate = "LateTuplesFromOutlierBoundaryBoltAggerate";
        public static String lateTuplesFromOutlierRoughBolt = "LateTuplesFromOutlierRoughBolt";
        public static String lateTuplesFromRouterBolt = "LateTuplesFromRouterBolt";
    }
}
