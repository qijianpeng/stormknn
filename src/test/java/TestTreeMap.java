import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by qijianpeng on 22/07/2017.
 */
public class TestTreeMap {
    public static void main(String[] args) {
        TreeMap<Long, String> treeMap = new TreeMap<>();
        treeMap.put(1L, "111");
        treeMap.put(11L, "222");
        treeMap.put(4L, "444");
        treeMap.put(3L, "333");


        Object[] arr =  treeMap.entrySet().toArray();
        treeMap.put(1L, "ssss");
        ((Map.Entry<Long, String>)arr[0]).setValue("ttttt");
        System.out.println(((Map.Entry<Long, String>)arr[0]).getValue());
        System.out.println(treeMap.get(1L));
//        System.out.println( arr);
    }
}
