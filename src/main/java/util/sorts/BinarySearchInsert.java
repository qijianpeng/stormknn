package util.sorts;



import java.util.Comparator;
import java.util.List;

/**
 * Created by qijianpeng on 26/05/2017.
 */
public class BinarySearchInsert<T extends Comparable<T>> {

    public static <T extends Comparable<T>>  boolean insert(T value, List<T> sortedList,
                                                            Comparator<T> comparator){
       int index = BinarySearchApproximate.find(value, sortedList, comparator, true);
       if (index < 0) return false;
       if(index >= sortedList.size())sortedList.add(value);
       else sortedList.add(index, value); return true;
    }

}
