package util.args;

import util.args.intervals.GenericInterval;
import util.args.intervals.IntegerInterval;
import util.args.intervals.Interval;

/**
 * Created by 633lab_Qi on 2018/3/2.
 */
public class TestArgUtil {
    public static void main(String[] args) {
        MainArgsHandler mah = MainArgsHandler.getHandler();
        String name = "Index.heigh";
        mah.permitVariable(name, MainArgsHandler.ONE_EXACTLY);
        mah.processMainArgs(args);
        System.out.println(mah.foundVariable(name));
        System.out.println(mah.getValuesFromVariable(name).toString());
    }
}
