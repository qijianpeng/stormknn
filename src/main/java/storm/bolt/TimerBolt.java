package storm.bolt;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.hdfs.common.AbstractHDFSWriter;
import org.apache.storm.hdfs.common.HDFSWriter;
import org.apache.storm.hdfs.common.Partitioner;
import org.apache.storm.hdfs.common.rotation.RotationAction;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.hdfs.bolt.AbstractHdfsBolt;
import storm.hdfs.bolt.HdfsBolt;
import storm.util.TupleUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

/**
 * Created by qijianpeng on 03/03/2018.
 * mail: jianpengqi@126.com
 */
public class TimerBolt extends HdfsBolt implements Serializable {
    private static final long serialVersionUID = 1L;
   // private static final Logger LOG = LoggerFactory.getLogger(TimerBolt.class);


    /**
     * Use "," for delimiter
     */
    RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("\t");

    /**
     * sync the filesystem after every 1k tuples.
     */
    SyncPolicy syncPolicy = new CountSyncPolicy(1000);

    /**
     * rotate files when they reach 10MB.
     */
    FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(128.0f, FileSizeRotationPolicy.Units.MB);

    /**
     *
     */
    FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath("/storm/timer/");
    {
        super.withFileNameFormat(fileNameFormat)
                .withRecordFormat(format)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);
    }

}
