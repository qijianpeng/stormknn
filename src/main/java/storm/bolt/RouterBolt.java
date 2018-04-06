package storm.bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.util.FieldUtils;
import storm.util.StreamUtils;
import util.Configuration;
import util.router.Router;
import vo.Point;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by qijianpeng on 18/06/2017.
 * Time-based tumbling window.
 * 目的是获取同一Slide的数据.
 * Tumbling window意为滚动窗口, 一个事件只能属于一个窗口, 窗口之间没有重叠.
 */
public class RouterBolt extends BaseWindowedBolt {
    private static final Logger LOG = LoggerFactory.getLogger(RouterBolt.class);
    private OutputCollector _collector;
    private static Router _router = null;

    private static long startTime = 0L;
    private static long endTime = 0L;
    private long interval = Long.MIN_VALUE;
    private long dataEndTime;
    private Map conf;
    private long totalStartTime = 0L;

    private boolean isWriteResults = true;
    private int _taskId;
    private int QUERY_NUM = 0;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //1. - declare streams id. each id represents a bolt.
        outputFieldsDeclarer.declareStream(StreamUtils.STREAM_ROUTER_TO_KNN,
                new Fields(/*FieldUtils.FIELD_ROUTER_PORT, */FieldUtils.FIELD_TIMESTAMP, FieldUtils.FIELD_POINTS));
        if (isWriteResults) {
            outputFieldsDeclarer.declareStream(StreamUtils.STREAM_TIME_COUNTER,
                    new Fields(FieldUtils.FIELD_TIME_RECODER));
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        conf = stormConf;
        _taskId = context.getThisTaskId();
        _collector = collector;
        _router = new Router(stormConf);
        _router.init();
        QUERY_NUM = Integer.valueOf(stormConf.get(Configuration.NAME_QUERY).toString());
        LOG.info("PREPARE_QUERY_NUM:\t{}", QUERY_NUM);
        dataEndTime = Long.valueOf(stormConf.get(Configuration.NAME_DATA_END_TIME).toString());
        isWriteResults = ! Boolean.valueOf(conf.get("no-write").toString());
    }

    @Override
    public void execute(TupleWindow inputWindow) {
        /**
         * 转发一个Slide的数据
         * <br/>
         * 每次调用execute方法 都会有一个新的 tupleWindow 产生,
         * 这里我们定义tupleWindow长度为 stream.slide.interval 个时间戳,
         * 即, 一个Slide就代表这里的一个Tumbling window.
         * @see Configuration.SLIDE_INTERVAL.
         */
        //LOG.info("Process the data of the current slide.");
        //1. 提取当前Slide的所有数据
        List<Tuple> currentWindow = inputWindow.get();
        if (currentWindow.size() == 0)return;
        _router.clearBuffer();//Do nothing
        startTime = System.currentTimeMillis();
        //1. 路由查询
        Set<Point> points = new HashSet<Point>();
        if (! Boolean.valueOf(conf.get("streaming-spout").toString()).booleanValue()) {
            int query_num = QUERY_NUM;
            for (Tuple tuple : currentWindow){
                //  collector.ack(tuple);
                List<List<String>> list = (List<List<String>>) tuple.getValueByField(FieldUtils.FIELD_POINTS);
                List<List<Point>> lists = new ArrayList<>();
                for (List<String> ps : list) {
                    lists.add(new ArrayList<>());
                    for (String p : ps) {
                        Point point = Point.newInstance(p);
                        if (query_num == -1){
                            point.setQuery(true);
                        }
                        //if (query_num == 0)query_num = QUERY_NUM;
                        lists.get(lists.size() - 1).add(point);
                    }
                }
                List<Point> pointList =
                        lists.stream().flatMap(List::stream).collect(Collectors.toList());
                Configuration.LATEST_TIME = pointList.get(pointList.size() - 1).getTimestamp();
                Configuration.LATEST_SLIDE_TIME = Configuration.LATEST_TIME / Configuration.SLIDE_INTERVAL;
                LOG.debug("Current time {} points size: {}",Configuration.LATEST_SLIDE_TIME, pointList.size());
                int i = 0;
                while (query_num > 0){
                    pointList.get(i).setQuery(true);
                    query_num--;
                    i++;
                }
                points.addAll(pointList);
               // _collector.ack(tuple);
            }
        }else{
            currentWindow.forEach((Tuple tuple) -> {
                String id = tuple.getValueByField(FieldUtils.POINT.ID).toString();
                Long timestamp = Long.parseLong(tuple.getValueByField(FieldUtils.POINT.TIME).toString());
                Double x = Double.parseDouble(tuple.getValueByField(FieldUtils.POINT.X).toString());
                Double y = Double.parseDouble(tuple.getValueByField(FieldUtils.POINT.Y).toString());
                boolean isQuery = Boolean.valueOf(tuple.getValueByField(FieldUtils.POINT.IS_QUERY).toString());
                Point point = new Point(id, new double[]{x, y}, timestamp);
                point.setQuery(isQuery);
                Configuration.LATEST_TIME = timestamp > Configuration.LATEST_TIME ? timestamp : Configuration.LATEST_TIME;
                Configuration.LATEST_SLIDE_TIME = Configuration.LATEST_TIME /  Configuration.SLIDE_INTERVAL;

                points.add(point);
              //  _collector.ack(tuple);
            });
        }
        int tmp = 0;
        for (Point point: points){
            if (point.isQuery())tmp++;
        }
        //LOG.info("Receive {} points.", points.size());
        //2. 路由器转发
        HashMap<Integer, Set<Point>> pointsRouting = new HashMap<>();
        int query_counter = 0;
        for (Point point : points){
            Map<Integer, Character> routs = _router.getIndexes(point);

            for (Map.Entry<Integer, Character> r : routs.entrySet()){
                Integer route = r.getKey();
                boolean isBoundary = r.getValue().equals('T') ? true : false;
                if (null == pointsRouting.get(route)){
                    pointsRouting.put(route, new HashSet<>());
                }

                Point p = new Point(point);
                p.setBoundary(isBoundary);
                if (isBoundary) {
                    p.setQuery(false);
                }
                pointsRouting.get(route).add(p);

                if (p.isQuery() && !isBoundary)query_counter++;
            }
        }
        //2.1 emitting tuples
        Iterator<Map.Entry<Integer, Set<Point>>> iter= pointsRouting.entrySet().iterator();
       tmp = 0;
        while (iter.hasNext()){
            Map.Entry<Integer, Set<Point>> message = iter.next();
            Integer routerPort = message.getKey();
            for (Point point: message.getValue()){
                if (point.isQuery() && ! point.isBoundary())tmp++;
            }
            Long timestamp = Configuration.LATEST_SLIDE_TIME;
            _collector.emit(StreamUtils.STREAM_ROUTER_TO_KNN, new Values(/*routerPort,*/
                    timestamp, message.getValue()));
           // iter.remove();
            endTime = System.currentTimeMillis();
            if (isWriteResults) {
                long per_100_time = ((endTime - startTime) * 100) / message.getValue().size();
                //long time_per_stamp = endTime - startTime;
                String val = "Router_Bolt_" + _taskId + " timer(per 100 points):\t" + per_100_time;
                _collector.emit(StreamUtils.STREAM_TIME_COUNTER, new Values(val));
                // LOG.info("Router_Bolt {} timer(per 100 points): {}", _taskId, per_100_time);
            }

        }
        LOG.info("QUERY_POINTS_SIZE emitting: {}, CurrentTime:\t{}", tmp, System.currentTimeMillis());
        if(isWriteResults) {
            String networkTimeStart = "NETWORK_TIME_START:\t" + System.currentTimeMillis() + "\tSlide_" + Configuration.LATEST_SLIDE_TIME;
            _collector.emit(StreamUtils.STREAM_TIME_COUNTER, new Values(networkTimeStart));
        }

    }
}