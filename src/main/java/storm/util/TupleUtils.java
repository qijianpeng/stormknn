package storm.util;

import org.apache.storm.Constants;
import org.apache.storm.tuple.Tuple;

import java.io.Serializable;

/**
 * Created by qijianpeng on 03/03/2018.
 * mail: jianpengqi@126.com
 */
public class TupleUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    private TupleUtils(){}
    public static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) && tuple.getSourceStreamId().equals(
                Constants.SYSTEM_TICK_STREAM_ID);
    }
}
