package util.sorts;


import java.util.Comparator;
import java.util.List;


public class BinarySearchApproximate<T extends Comparable> {


    private static final int SWITCH_TO_BRUTE_FORCE = 200;

    //private static T[] sorted = null;
    // Assuming the array is sorted
    public static final  <T extends Comparable<T>>  int find(T value, T[] sorted,
                                                             Comparator<T> comparator, boolean optimize) {
        return recursiveFind(value,sorted, 0, sorted.length - 1, comparator, optimize);
    }

    private static  <T extends Comparable<T>>  int recursiveFind(T value, T[] sorted, int start,
                                                                 int end, boolean optimize) {
        if (end - start <= 1) {
            T lastValue = sorted[end]; // start==end
            if (value.compareTo(lastValue) <= 0)
                return end; // start==end

            else return Integer.MAX_VALUE;
        }

        int low = start;
        int high = end + 1; // zero indexed, so add one.
        int middle = low + ((high - low) / 2);

        T middleValue = sorted[middle];
        if (value.equals(middleValue))
            return middle;
        if (value.compareTo(middleValue) > 0) {
            if (optimize && (end - middle) <= SWITCH_TO_BRUTE_FORCE)
                return linearSearch(value,sorted, middle + 1, end);
            return recursiveFind(value, sorted,middle + 1, end, optimize);
        }
        if (optimize && (end - middle) <= SWITCH_TO_BRUTE_FORCE)
            return linearSearch(value, sorted, start, middle - 1);
        return recursiveFind(value, sorted, start, middle - 1, optimize);
    }

    private static  <T extends Comparable<T>>  int recursiveFind(T value, T[] sorted, int start,
                                           int end, Comparator<T> comparator, boolean optimize) {
        if (end - start <= 1) {
            T rightValue = sorted[end]; // start==end
            T leftValue = sorted[start];
            if(comparator.compare(value, rightValue) <= 0 &&
                    comparator.compare(value, leftValue) >= 0)return end;//ascending order
            if (start == end){
                if (comparator.compare(value, leftValue) <= 0) return end;
                if (comparator.compare(value, leftValue) >= 0) return end +1;
            }
        }

        int low = start;
        int high = end + 1; // zero indexed, so add one.
        int middle = low + ((high - low) / 2);

        T middleValue = sorted[middle];

        if (value.equals(middleValue))
            return middle;
        if (comparator.compare(value, middleValue) > 0) {
            if (optimize && (end - middle) <= SWITCH_TO_BRUTE_FORCE)
                return linearSearch(value, sorted, middle , end, comparator);
            return recursiveFind(value, sorted, middle, end, comparator, optimize);
        }
        if (optimize && (end - middle) <= SWITCH_TO_BRUTE_FORCE)
            return linearSearch(value, sorted, start, middle , comparator);
        return recursiveFind(value, sorted, start, middle , comparator, optimize);
    }

    private static final  <T extends Comparable<T>>  int linearSearch(T value, T[] sorted, int start, int end) {
        for (int i = start; i <= end ; i++) {
            T iValue = sorted[i];
            if (value.compareTo(iValue) > 0)continue;
            else return i;
        }
        return Integer.MAX_VALUE;
    }
    private static final  <T extends Comparable<T>>  int linearSearch(T value, T[] sorted,
                                                                      int start, int end, Comparator<T> comparator) {
        for (int i = start; i <= end ; i++) {
            T iValue = sorted[i];
            if (comparator.compare(value, iValue) > 0)continue;
            else return i;
        }
        return Integer.MAX_VALUE;
    }

//-----------List Methods
    public static final  <T extends Comparable<T>>  int find(T value, List<T> sorted,
                                                             Comparator<T> comparator, boolean optimize) {
        return recursiveFind(value,sorted, 0, sorted.size() - 1, comparator, optimize);
    }


    private static  <T extends Comparable<T>>  int recursiveFind(T value, List<T> sorted, int start,
                                                                 int end, Comparator<T> comparator, boolean optimize) {
        if (end - start <= 1) {
            T rightValue = sorted.get(end); // start==end
            T leftValue = sorted.get(start);
            if(comparator.compare(value, rightValue) <= 0 &&
                    comparator.compare(value, leftValue) >= 0)return end;//ascending order
            if (start == end){
                if (comparator.compare(value, leftValue) <= 0) return end;
                if (comparator.compare(value, leftValue) >= 0) return end +1;
            }
        }

        int low = start;
        int high = end + 1; // zero indexed, so add one.
        int middle = low + ((high - low) / 2);

        T middleValue = sorted.get(middle);

        if (value.equals(middleValue))
            return middle;
        if (comparator.compare(value, middleValue) > 0) {
            if (optimize && (end - middle) <= SWITCH_TO_BRUTE_FORCE)
                return linearSearch(value,sorted, middle , end, comparator);
            return recursiveFind(value, sorted,middle , end, comparator, optimize);
        }
        if (optimize && (end - middle) <= SWITCH_TO_BRUTE_FORCE)
            return linearSearch(value, sorted, start, middle , comparator);
        return recursiveFind(value, sorted, start, middle , comparator, optimize);
    }

    private static final  <T extends Comparable<T>>  int linearSearch(T value, List<T> sorted,
                                                                      int start, int end, Comparator comparator) {
        for (int i = start; i <= end ; i++) {
            T iValue = sorted.get(i);
            if (comparator.compare(value, iValue) > 0)continue;
            else return i;
        }
        return Integer.MAX_VALUE;
    }
}
