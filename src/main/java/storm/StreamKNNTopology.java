package storm;

import com.google.common.collect.ImmutableList;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.KillOptions;
import org.apache.storm.generated.Nimbus;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.SimpleFileNameFormat;
import org.apache.storm.hdfs.spout.Configs;
import org.apache.storm.thrift.TException;
import org.apache.storm.topology.TopologyBuilder;

import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.khelekore.prtree.DistanceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.bolt.*;
import storm.hdfs.spout.HdfsSpout;
import storm.util.FieldUtils;
import storm.util.StreamUtils;
import util.Configuration;
import util.args.MainArgsHandler;
import util.args.intervals.GenericInterval;
import util.args.intervals.Interval;
import vo.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by qijianpeng on 15/06/2017.
 */
public class StreamKNNTopology {
    private static Logger LOG = LoggerFactory.getLogger(StreamKNNTopology.class);


    public static void main(String[] args) throws TException, InterruptedException, IOException {

        /**
         *Topology settings.
         */
        //2 - storm config.
        Config conf = new Config();
        conf.setNumWorkers(20);
      //  conf.put(Config.TOPOLOGY_FALL_BACK_ON_JAVA_SERIALIZATION, true);
      //  conf.put(Config.TOPOLOGY_SKIP_MISSING_KRYO_REGISTRATIONS, true);
      //  conf.setFallBackOnJavaSerialization(true);
        conf.setNumAckers(1);
        conf.setMaxTaskParallelism(20);
       // conf.setDebug(true);
        //maximum amount of time a message has to complete before it's considered failed
        conf.setMessageTimeoutSecs(30);
        conf.setTopologyWorkerMaxHeapSize(2048);
        conf.setMaxSpoutPending(50000);
        conf.put(Config.TOPOLOGY_BACKPRESSURE_ENABLE, true);

        init(conf, args);
        boolean isWriteResults = ! Boolean.valueOf(conf.get("no-write").toString());
        // conf.registerMetricsConsumer(LoggingMetricsConsumer.class);
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        String topologyName = "KnnTopology";
        /**
         *0). HDFSSpout, 从HDFS按Slide读取数据, Spout数量设置为1.
         */
        if(null == args || args.length == 0) {
            Configuration.HDFS_URI = "hdfs://localhost:9000";
        }
        HdfsSpout hdfsSpout = new HdfsSpout()
                .setHdfsUri(Configuration.HDFS_URI)
                .setArchiveDir(Configuration.HDFS_ARCHIVE_DIR)
                .setSourceDir(Configuration.HDFS_SOURCE_DIR)
                .setBadFilesDir(Configuration.HDFS_BADFILES_DIR)
                .setReaderType(Configs.TEXT);
        if (Boolean.valueOf(conf.get("streaming-spout").toString()).booleanValue()){
            hdfsSpout.withOutputFields(new String[]
                    {FieldUtils.POINT.ID, FieldUtils.POINT.TIME,
                            FieldUtils.POINT.X, FieldUtils.POINT.Y, FieldUtils.POINT.IS_QUERY})
                    .withDelimiter("\t");
        }else {
            hdfsSpout.withOutputFields(new String[]
                    {FieldUtils.FIELD_TIMESTAMP, FieldUtils.FIELD_POINTS})
                    .withDelimiter("\t");
        }

        topologyBuilder.setSpout(StreamUtils.SPOUT_HDFS, hdfsSpout, 1);


        /**
         * 1). 路由模块加入, topology中只有一个路由器. 从HdfsSpout读取slide数据.
         */
        RouterBolt routerBolt = new RouterBolt();
        routerBolt.withTimestampField(FieldUtils.FIELD_TIMESTAMP)
                .withTumblingWindow(new BaseWindowedBolt.Duration(Configuration.SLIDE_INTERVAL, TimeUnit.MILLISECONDS))
                .withLag(new BaseWindowedBolt.Duration(0, TimeUnit.MILLISECONDS))
                .withWatermarkInterval(new BaseWindowedBolt.Duration(0, TimeUnit.MILLISECONDS))
        ;
        topologyBuilder.setBolt(StreamUtils.BOLT_ROUTER,routerBolt, 1)
                .allGrouping(StreamUtils.SPOUT_HDFS);

        /**
         * 2). KNN Bolt receives points stream & calculates top K neighbors.
         */
        KNNBolt knnBolt = new KNNBolt();
        topologyBuilder.setBolt(StreamUtils.BOLT_KNN, knnBolt, Configuration.KNN_BOLT_NUM)
                        .localOrShuffleGrouping(StreamUtils.BOLT_ROUTER, StreamUtils.STREAM_ROUTER_TO_KNN);//.fieldsGrouping(StreamUtils.BOLT_ROUTER,StreamUtils.STREAM_ROUTER_TO_KNN ,new Fields(FieldUtils.FIELD_ROUTER_PORT));

        if (isWriteResults) {
            ResultHandlerBolt resultHandlerBolt = new ResultHandlerBolt();
            String fileName = "$COMPONENT-$TASK." + conf.get(Configuration.NAME_KNN_BOLT_NUM).toString()
                    + "-KNN_BOLTS."
                    + conf.get(Configuration.NAME_QUERY) + "-queries."
                    + conf.get(Configuration.NAME_K) + "-K."
                    + conf.get(Configuration.NAME_INDEX_HIGHT) + "-indexHeight"
                    + ".txt";
            FileNameFormat fileNameFormat = new SimpleFileNameFormat().withPath("/storm/results/")
                    .withName(fileName);
            resultHandlerBolt.withFsUrl(Configuration.HDFS_URI)
                    .withFileNameFormat(fileNameFormat);
            topologyBuilder.setBolt(StreamUtils.STREAM_RESULT, resultHandlerBolt, Configuration.RESULTS_BOLTS_NUM)
                    .fieldsGrouping(StreamUtils.BOLT_KNN, StreamUtils.STREAM_RESULT,
                            new Fields(FieldUtils.FIELD_TIMESTAMP));

            TimerBolt timerBolt = new TimerBolt();
            timerBolt.withFsUrl(Configuration.HDFS_URI);
            topologyBuilder.setBolt(StreamUtils.STREAM_TIME_COUNTER, timerBolt,1)
                    .allGrouping(StreamUtils.BOLT_ROUTER, StreamUtils.STREAM_TIME_COUNTER)
                    .allGrouping(StreamUtils.BOLT_KNN, StreamUtils.STREAM_TIME_COUNTER);
        }

        /**
         * 4). 创建Topology并提交.
         */
        StormTopology stormTopology =  topologyBuilder.createTopology();

        if (null != args && args.length > 0){

            StormSubmitter.submitTopologyWithProgressBar("stream_knn", conf, stormTopology);
        }else {
            LocalCluster localCluster = new LocalCluster();

            localCluster.submitTopology(topologyName,conf, stormTopology);
            Thread.sleep(400000);
            localCluster.shutdown();
        }
    }
    private static void kill(Nimbus.Iface client, String topologyName) throws TException {
        KillOptions opts = new KillOptions();
        opts.set_wait_secs(0);
        client.killTopologyWithOpts(topologyName,opts);
    }

    /**
     * 序列化器注册
     * @param config
     */
    static void init(Config config, String[] args){
        //Log.TRACE();
       // config.registerSerialization(Point.class, PointSerializer.class);
        config.registerSerialization(TreeMap.class);
        config.registerSerialization(LinkedList.class);
        config.registerSerialization(HashSet.class);
        config.registerSerialization(ArrayList.class);

        config.registerSerialization(Point.class);

        config.registerSerialization(double[][].class);
        config.registerSerialization(double[].class);
        config.registerSerialization(ImmutableList.class);
        config.registerSerialization(DistanceResult.class);

        config.put(Configuration.NAME_BOUNDARY_LENGTH_SCALE, Configuration.BOUNDARY_LENGTH_SCALE);
        config.put(Configuration.NAME_BRANCH_FACTOR, Configuration.BRANCH_FACTOR);
        config.put(Configuration.NAME_INDEX_HIGHT, Configuration.INDEX_HIGHT);
        config.put(Configuration.NAME_K, Configuration.K);
        config.put(Configuration.NAME_KNN_BOLT_NUM, Configuration.KNN_BOLT_NUM);
        config.put(Configuration.NAME_DATA_END_TIME, Configuration.DATA_ENDTIME);

        String useStreamingSpout = "streaming-spout";
        String no_results = "no-write";//WriteToHDFS

        config.put(useStreamingSpout, false);
        config.put(no_results, false);
        config.put(Configuration.NAME_QUERY, 1000);
        if (null != args && args.length > 0) {

            MainArgsHandler mah = MainArgsHandler.getHandler();

            mah.permitVariable(Configuration.NAME_BOUNDARY_LENGTH_SCALE, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_BRANCH_FACTOR, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_INDEX_HIGHT, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_K, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_KNN_BOLT_NUM, MainArgsHandler.ZERO_OR_ONE);
            mah.permitVariable(Configuration.NAME_DATA_END_TIME, MainArgsHandler.ZERO_OR_ONE);
            mah.permitFlag(no_results);
            mah.permitFlag(useStreamingSpout);
            mah.permitVariable(Configuration.NAME_QUERY, MainArgsHandler.ZERO_OR_ONE);

            String acker_num = "acker.num";
            mah.permitVariable(acker_num, MainArgsHandler.ZERO_OR_ONE);

            String results_bolts_num = "results.bolts.num";
            mah.permitVariable(results_bolts_num, MainArgsHandler.ZERO_OR_ONE);

            mah.processMainArgs(args);

            if (mah.foundVariable(Configuration.NAME_BOUNDARY_LENGTH_SCALE)){
                Configuration.BOUNDARY_LENGTH_SCALE =  Double.parseDouble(mah.getValuesFromVariable(Configuration.NAME_BOUNDARY_LENGTH_SCALE).get(0));
                config.put(Configuration.NAME_BOUNDARY_LENGTH_SCALE, Configuration.BOUNDARY_LENGTH_SCALE);
            }
            if (mah.foundVariable(Configuration.NAME_BRANCH_FACTOR)){
                Configuration.BRANCH_FACTOR =  Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_BRANCH_FACTOR).get(0));
                config.put(Configuration.NAME_BRANCH_FACTOR, Configuration.BRANCH_FACTOR);
            }
            if (mah.foundVariable(Configuration.NAME_INDEX_HIGHT)){
                Configuration.INDEX_HIGHT =  Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_INDEX_HIGHT).get(0));
                config.put(Configuration.NAME_INDEX_HIGHT, Configuration.INDEX_HIGHT);
            }
            if (mah.foundVariable(Configuration.NAME_KNN_BOLT_NUM)){
                Configuration.KNN_BOLT_NUM =  Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_KNN_BOLT_NUM).get(0));
                config.put(Configuration.NAME_KNN_BOLT_NUM, Configuration.KNN_BOLT_NUM);
            }
            if (mah.foundVariable(Configuration.NAME_K)){
                Configuration.K =  Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_K).get(0));
                config.put(Configuration.NAME_K, Configuration.K);
            }
            if (mah.foundVariable(Configuration.NAME_DATA_END_TIME)){
                Configuration.DATA_ENDTIME = Long.parseLong(mah.getValuesFromVariable(Configuration.NAME_DATA_END_TIME).get(0));
                config.put(Configuration.NAME_DATA_END_TIME, Configuration.DATA_ENDTIME);
            }
            if (mah.foundVariable(acker_num)){
                config.setNumAckers(Integer.parseInt(mah.getValuesFromVariable(acker_num).get(0)));
            }else {
                config.setNumAckers(0);
            }
            if (mah.foundFlag(useStreamingSpout)){
                config.put(useStreamingSpout, true);
            }
            if (mah.foundFlag(no_results)){
                config.put(no_results, true);
            }else config.put(no_results, false);
            if (mah.foundVariable(Configuration.NAME_QUERY)){
                config.put(Configuration.NAME_QUERY,
                        Integer.parseInt(mah.getValuesFromVariable(Configuration.NAME_QUERY).get(0).toString()));
            }else{
                config.put(Configuration.NAME_QUERY, -1);
            }
            if (mah.foundVariable(results_bolts_num)){
                Configuration.RESULTS_BOLTS_NUM = Integer.valueOf(
                        mah.getValuesFromVariable(results_bolts_num).get(0).toString()
                );
            }
        }
    }

}
