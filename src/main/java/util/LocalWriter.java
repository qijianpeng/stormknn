package util;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Created by qijianpeng on 03/08/2017.
 * mail: jianpengqi@126.com
 */
public class LocalWriter {
    private LocalWriter(){}


    /**
     * 向file写入一个异常
     * @param outlier 异常点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param append 是否追加
     */
    static void append(Object outlier, long startTime, long endTime, File to, boolean append) throws IOException {
        String str = startTime + "\t" + endTime + "\t" + outlier.toString()+"\n";
        Files.append(str, to, Charset.forName("UTF-8"));
    }

    /**
     * 向writer写入异常点集合.
     * @param outliers 异常点集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param append 是否追加
     */
   static void append(Set<Object> outliers, long startTime, long endTime, File to, boolean append) throws IOException {
       StringBuffer sb = new StringBuffer();
       for (Object obj : outliers){
            sb.append( startTime + "\t" + endTime + "\t" + obj.toString() + "\n" );
       }
       Files.append(sb.toString(), to, Charset.forName("UTF-8"));
   }


    /**
     * 向file写入一个异常
     * @param outlier 异常点
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    static void write(Object outlier, long startTime, long endTime, File to) throws IOException {
        String str = startTime + "\t" + endTime + "\t" + outlier.toString()+"\n";
        Files.write(str, to, Charset.forName("UTF-8"));
    }

    /**
     * 向writer写入异常点集合.
     * @param outliers 异常点集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    static void write(Set<Object> outliers, long startTime, long endTime, File to) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (Object obj : outliers){
            sb.append( startTime + "\t" + endTime + "\t" + obj.toString() + "\n" );
        }
        Files.write(sb.toString(), to, Charset.forName("UTF-8"));
    }

}
