package storm.util;

/**
 * Created by qijianpeng on 27/06/2017.
 */
public class FieldUtils {
    public static final String FIELD_TIMESTAMP = "POINT_TIME";
    public static final String FIELD_POINTS = "POINTS_LIST";
    public static final String FIELD_POINT = "POINT";
    public static final String FIELD_ROUTER_PORT = "ROUTER_PORT";

    public static final String FIELD_TIME_RECODER = "TIMERECODER";

    public static final String FIELD_KNN = "KNN";
    public class POINT{
        public static final String ID = "POINT_ID";
        public static final String TIME = "POINT_TIME";
        public static final String X = "POINT_X";
        public static final String Y = "POINT_Y";
        public static final String IS_QUERY = "IS_QUERY";
    }


    public class RESULTS{
        public static final String START_TIME = "START_SLIDE_TIME";
        public static final String END_TIME = "END_SLIDE_TIME";
        public static final String OUTLIER = "OUTLIER";
        public static final String IDENTIFY_TIME = "IDENTIFY_TIME";
        public static final String OUTLIER_TIME = "OUTLIER_TIME";
    }
}
