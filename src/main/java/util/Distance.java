package util;


/**
 * Created by qijianpeng on 16/07/2017.
 */
public class Distance {
    public static Double euclideanDistance(double[] p1, double[] p2){
        double sum = 0.0;
        for (int i = 0; i < p1.length; i++){
           sum += ((p1[i] - p2[i]) * (p1[i] - p2[i]));
        }
        return Math.sqrt(sum);
    }

    public static Double distance(double[] p1, double[] p2){
        return euclideanDistance(p1, p2);
    }
}
