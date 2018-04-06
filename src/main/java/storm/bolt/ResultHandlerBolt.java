package storm.bolt;

import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.*;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.topology.OutputFieldsDeclarer;
import storm.util.StreamUtils;

/**
 * Created by qijianpeng on 04/08/2017.
 * mail: jianpengqi@126.com
 */
public class ResultHandlerBolt extends HdfsBolt {
    /**
     * Use "\t" for delimiter
     */
    RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("\t");

    /**
     * sync the filesystem after every 1k tuples.
     */
    SyncPolicy syncPolicy = new CountSyncPolicy(1000);

    /**
     * rotate files when they reach 10MB.
     */
    FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(2.0f, FileSizeRotationPolicy.Units.GB);

    /**
     *
     */

    {
        super.withRecordFormat(format)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);


    }



}
