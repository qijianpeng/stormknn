package util.sorts;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Quicksort is a sorting algorithm which, on average, makes O(n*log n) comparisons to sort
 * n items. In the worst case, it makes O(n^2) comparisons, though this behavior is
 * rare. Quicksort is often faster in practice than other algorithms.
 *
 * Family: Divide and conquer.
 * Space: In-place.
 * Stable: False.
 *
 * Average case = O(n*log n), Worst case = O(n^2), Best case = O(n) [three-way partition and equal keys]
 *
 * http://en.wikipedia.org/wiki/Quick_sort
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 * ---
 * 2017.05.24 SEQUENCE, REVERSE sort
 */
public class QuickSort<T extends Comparable<T>> {

    private static final Random RAND = new Random();

    public static enum PIVOT_TYPE {
        FIRST, MIDDLE, RANDOM
    }

    public static PIVOT_TYPE type = PIVOT_TYPE.RANDOM;

    private QuickSort() { }

    public static <T extends Comparable<T>> List<T> sort(PIVOT_TYPE pivotType, List<T> unsorted) {
        int pivot = 0;
        if (pivotType == PIVOT_TYPE.MIDDLE) {
            pivot = unsorted.size()/2;
        } else if (pivotType == PIVOT_TYPE.RANDOM) {
            pivot = getRandom(unsorted.size());
        }
        sort(pivot, 0, unsorted.size() - 1, unsorted);
        return unsorted;
    }

    public static <T extends Comparable<T>> List<T> sort(PIVOT_TYPE pivotType, List<T> unsorted,
                                                     Comparator<T> comparator) {
        int pivot = 0;
        if (pivotType == PIVOT_TYPE.MIDDLE) {
            pivot = unsorted.size()/2;
        } else if (pivotType == PIVOT_TYPE.RANDOM) {
            pivot = getRandom(unsorted.size());
        }
        sort(pivot, 0, unsorted.size() - 1, unsorted, comparator);
        return unsorted;
    }
    private static <T extends Comparable<T>> void sort(int index, int start, int finish, List<T> unsorted) {
        int pivotIndex = start + index;
        T pivot = unsorted.get(pivotIndex);
        int s = start;
        int f = finish;
        while (s <= f) {
            while (unsorted.get(s).compareTo(pivot) < 0)
                s++;
            while (unsorted.get(f).compareTo(pivot) > 0)
                f--;
            if (s <= f) {
                swap(s, f, unsorted);
                s++;
                f--;
            }
        }
        if (start < f) {
            pivotIndex = getRandom((f - start) + 1);
            sort(pivotIndex, start, f, unsorted);
        }
        if (s < finish) {
            pivotIndex = getRandom((finish - s) + 1);
            sort(pivotIndex, s, finish, unsorted);
        }
    }

    private static <T extends Comparable<T>> void sort(int index, int start, int finish,
                                                       List<T> unsorted, Comparator<T> comparator) {
        int pivotIndex = start + index;
        T pivot = unsorted.get(pivotIndex);
        int s = start;
        int f = finish;
        while (s <= f) {
            while (comparator.compare(unsorted.get(s), pivot) < 0)
                s++;
            while (comparator.compare(unsorted.get(f), pivot) > 0)
                f--;
            if (s <= f) {
                swap(s, f, unsorted);
                s++;
                f--;
            }
        }
        if (start < f) {
            pivotIndex = getRandom((f - start) + 1);
            sort(pivotIndex, start, f, unsorted, comparator);
        }
        if (s < finish) {
            pivotIndex = getRandom((finish - s) + 1);
            sort(pivotIndex, s, finish, unsorted, comparator);
        }
    }
    private static final int getRandom(int length) {
        if (type == PIVOT_TYPE.RANDOM && length > 0)
            return RAND.nextInt(length);
        if (type == PIVOT_TYPE.FIRST && length > 0)
            return 0;
        return length / 2;
    }

    private static <T extends Comparable<T>> void swap(int index1, int index2, List<T> unsorted) {
        T index2Element = unsorted.get(index1);
        unsorted.set(index1, unsorted.get(index2));
        unsorted.set(index2, index2Element);
    }
}
