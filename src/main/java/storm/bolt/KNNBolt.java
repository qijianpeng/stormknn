package storm.bolt;

import org.apache.commons.math3.util.Pair;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.khelekore.prtree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.util.FieldUtils;
import storm.util.StreamUtils;
import storm.util.TupleUtils;
import util.Configuration;
import vo.Point;
import vo.Point2DConverter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by qijianpeng on 01/03/2018.
 * mail: jianpengqi@126.com
 */
public class KNNBolt extends BaseBasicBolt implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = LoggerFactory.getLogger(KNNBolt.class);
    private static long startTime = 0L;
    private static long endTime = 0L;
    private PRTree<Point> tree;
    private Point2DConverter converter;
    private  DistanceCalculator<Point> dc;
    private NodeFilter<Point> acceptAll;
    private Integer K;
    private Integer BRANCH_FACTOR;
    private long dataEndTime;
    private int _taskId;
    //private long interval = Long.MIN_VALUE;
    private boolean isWriteResults = true;
    private int  QUERY_NUM ;
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        _taskId = context.getThisTaskId();
        super.prepare(stormConf, context);
        converter = new Point2DConverter();
        dc = new Point();
        acceptAll = new AcceptAll<>();
        K = Integer.valueOf(stormConf.get(Configuration.NAME_K).toString());
        BRANCH_FACTOR = Integer.valueOf(stormConf.get(Configuration.NAME_BRANCH_FACTOR).toString());
        dataEndTime = Long.valueOf(stormConf.get(Configuration.NAME_DATA_END_TIME).toString());

        isWriteResults = ! Boolean.valueOf(stormConf.get("no-write").toString());
        QUERY_NUM = Integer.valueOf(stormConf.get(Configuration.NAME_QUERY).toString());
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
       // FieldUtils.FIELD_ROUTER_PORT, FieldUtils.FIELD_TIMESTAMP, FieldUtils.FIELD_POINTS
        if (TupleUtils.isTickTuple(input))return;

       //mv Integer routerPort = input.getIntegerByField(FieldUtils.FIELD_ROUTER_PORT);
        Long timestamp = input.getLongByField(FieldUtils.FIELD_TIMESTAMP);
        Set<Point> points = (HashSet<Point>)input.getValueByField(FieldUtils.FIELD_POINTS);

        if (isWriteResults) {
            String networkTimeEnd = "NETWORK_TIME_END:\t" + System.currentTimeMillis() +
                    "\t Slide_" + timestamp + "_KNN_BoltID_" + _taskId;
            collector.emit(StreamUtils.STREAM_TIME_COUNTER, new Values(networkTimeEnd));
        }
        startTime = System.currentTimeMillis();
        LOG.debug("KNN Analyzing,slide {}, size {}.",timestamp, points.size());
        tree = new PRTree<Point> (this.converter, BRANCH_FACTOR);
        tree.load(points);
        int query_counter=0;
        for (Point point: points) {
            if (point.isBoundary() || !point.isQuery())continue;
            query_counter++;
            //timer counter
            List<DistanceResult<Point>> res = tree.nearestNeighbour(dc, acceptAll, K + 1, point);
            if ( isWriteResults ){
                List<Point> topK = res.stream().map(pointDistanceResult ->
                        pointDistanceResult.get()).collect(Collectors.toList());
                List<Double> topKDistance = res.stream().map(pointDistanceResult ->
                        pointDistanceResult.getDistance()).collect(Collectors.toList());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < topK.size(); i++){
                    sb.append(topK.get(i).toString() /*+ topKDistance.get(i)*/ +"\t");
                }

           //   LOG.info("Emitting tuple to results_bolt, {}", point.toString());
                collector.emit(StreamUtils.STREAM_RESULT, new Values(timestamp,point.toString(), sb.toString()));
            }
        }
        LOG.info("QuerySize:\t{}\t, timestamp: {}, CurrentTime:\t{}", query_counter, timestamp, System.currentTimeMillis());
        if (isWriteResults) {
            endTime = System.currentTimeMillis();
            long per_100_time = (endTime - startTime) * 100 / points.size();
            //LOG.info("KNN_BoltID_{} timer(per 100 queries):{}", _taskId, per_100_time);
            String str = "KNN_BoltID_" + _taskId + " time(per 100 queries):\t" + per_100_time
                    + "\t size:\t" + points.size();
            Values val = new Values(str);
            collector.emit(StreamUtils.STREAM_TIME_COUNTER, val);
        }


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (isWriteResults) {
            declarer.declareStream(StreamUtils.STREAM_RESULT, new Fields(FieldUtils.FIELD_TIMESTAMP, FieldUtils.FIELD_POINT, FieldUtils.FIELD_KNN));
            declarer.declareStream(StreamUtils.STREAM_TIME_COUNTER, new Fields(FieldUtils.FIELD_TIME_RECODER));
        }
    }
}
