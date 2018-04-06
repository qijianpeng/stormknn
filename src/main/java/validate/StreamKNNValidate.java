package validate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * 完成异常检测结果的验证.
 * 正确性上包含对三个指标的考查.
 *
 *
 *
 * Created by qijianpeng on 16/08/2017.
 * mail: jianpengqi@126.com
 */
public class StreamKNNValidate implements Serializable, EffectivenessEvaluation {
    private static final long serialVersionUID = 1L;

   /* *//**
     * 算法结果文件
     *//*
     private String _testResultPath = "";
    *//**
     * 正确结果文件
     *//*
     private String _rightResultPath = "";
*/
    private Map<String, Map<String, Set<String>>> _testResults ;
    private Map<String, Map<String, Set<String>>> _correctResults ;

    public static void main(String[] args) throws IOException {
        StreamKNNValidate sov = new StreamKNNValidate();
        String correctPath = "/Users/qijianpeng/Downloads/StreamKNN/Serial_results_beijingCircle2_20180302_30t_top20.txt";
        String resultPath = "/Users/qijianpeng/Downloads/StreamKNN/STREAM_RESULT.12-KNN_BOLTS.-1-queries.20-K.5-indexHeight_0.3scale.txt";
        double precision = 0.0, recall = 0.0;
        final int TIMESTAMPS = 30;

        for (timestamp = 1 ; timestamp <=TIMESTAMPS; timestamp ++) {
            sov._correctResults = sov.load(correctPath);
            sov._testResults = sov.load(resultPath);
            double pre = sov.precision();
            precision += pre;
            System.out.print("P: " + pre /*+ "， Serial size: " +
                    sov._correctResults.size() + ", Parallel size: " + sov._testResults.size()*/);
            double rec = sov.recall();
            recall += rec;
            System.out.println("\tR: " + rec+"\t Time:"+timestamp);

      /*  HashSet<String> tmp = new HashSet<>(sov._testResults);
        sov._testResults.retainAll(sov._correctResults);
        System.out.println("Retain size:"+ sov._testResults.size());
        tmp.removeAll(sov._testResults);
        System.out.println("Contents: "+tmp);*/
        }
        System.gc();
        System.out.println("Precision\tRecall");
        System.out.println(precision/TIMESTAMPS+"\t"+recall/TIMESTAMPS +"");

    }
    public static int timestamp = 1;
    public  Map<String, Map<String, Set<String>>> load(String path) throws IOException {
        //List<String> list = FileUtils.readLines(new File(path),"utf-8");
       // System.out.println("before delete duplicate recorders: " + list.size());
        Map<String, Map<String, Set<String>>> res = new HashMap<>();
        int counter = 0;
        LineIterator li = FileUtils.lineIterator(new File(path), "utf-8");
        while (li.hasNext()){

            String line = li.nextLine().trim();
            String[] strs = line.split("\t");
            String time = strs[0];
            if (!time.equalsIgnoreCase(timestamp+""))continue;
            counter++;
            if (null == res.get(time))res.put(time, new HashMap<String, Set<String>>());
            String id = strs[1];
            Set<String> topks = new HashSet<>();
            for (int i = 2; i < strs.length; i ++){
                topks.add(strs[i]);
            }
            res.get(time).put(id, topks);

        }
        System.out.println("Total: "+counter+"\t refine: "+ res.get(timestamp+"").size());
        return  res;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public double recall() {
        //Set<String> t =  intersection(_testResults, _correctResults);
        double recall = 0.0;
        for (String time : _testResults.keySet()){
            Map<String, Set<String>> res1 = _testResults.get(time);
            Map<String, Set<String>> res2 = _correctResults.get(time);

            double r = 0.0;
            for (String id : res1.keySet()){
                int count = 0;
                for (String neighbor : res1.get(id)){
                    if (res2.get(id).contains(neighbor))count ++;
                }
                r = r + count*1.0 / res2.get(id).size();
            }
            r = r / res2.size();
            recall += r;
        }
        recall /= _correctResults.size();

        return recall ;
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public double precision() {
        double precision = 0.0;
        for (String time : _testResults.keySet()){
            Map<String, Set<String>> res1 = _testResults.get(time);
            double pre = 0.0;
            for (String id : res1.keySet()){
                int count = 0;
                for (String neighbor: res1.get(id)){
                    if (_correctResults.get(time).get(id).contains(neighbor))count++;
                }
                pre = pre + count * 1.0 / res1.get(id).size();
            }
            pre /= res1.size();
            precision += pre;
        }
        precision /= _testResults.size();
        return precision;
    }
    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public double accuracy() {
        return 0;
    }

}
