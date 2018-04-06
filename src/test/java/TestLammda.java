import org.apache.storm.shade.com.google.common.collect.Collections2;
import org.apache.storm.tuple.Fields;
import vo.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by qijianpeng on 19/06/2017.
 */
public class TestLammda {
    public static void main(String[] args) {
       /* String[] strs = new String[]{"timestamp","values"};

        System.out.println(strs);


        List<List<Integer>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.get(0).add(1);
        list.get(0).add(2);
        list.get(0).add(3);
        list.get(1).add(4);
        list.get(1).add(5);
      *//*  List<Integer> result =  list.stream().collect(ArrayList::new,(response,
           element)-> response.addAll(element) , (response1, response2)->{
           response1.addAll(response2);
        });*//*

        List<Integer> result = list.stream().collect(ArrayList::new,
                (l, e) -> l.addAll(e), List::addAll);
        result.stream().forEach(e -> {
            System.out.println(e);
        });*/

        /*HashMap<Integer, HashSet> currentGrid = new HashMap<>();
        currentGrid.computeIfAbsent(1, k -> new HashSet<String>()).add("SS");
        currentGrid.computeIfAbsent(1, k -> new HashSet<String>()).add("WW");
        System.out.println(currentGrid.get(1).toString());
        */

       /* List<String>  list = new ArrayList<>();
        list.forEach(s -> System.out.println(s));
        */
        HashSet<Integer> set1 =  org.apache.storm.shade.com.google.common.collect.Sets.newHashSet(1,3,5,7);
        HashSet<Integer> set2 =  org.apache.storm.shade.com.google.common.collect.Sets.newHashSet(2,3,4,7);

        HashSet<Integer> set3 = new HashSet<>(set1);
        System.out.println(""+set3.retainAll(set2)+ set1);
        System.out.println(set1);
        System.out.println(set2);
        System.out.println(set3);




    }
}
