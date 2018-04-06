package validate;

/**
 * Created by qijianpeng on 16/08/2017.
 * mail: jianpengqi@126.com
 */
public interface EffectivenessEvaluation {

    /**
     * 计算召回率.
     * 计算公式: Recall = Results intersect CorrectInDataBase / CorrectInDataBase
     * @return
     */
    public double recall();

    /**
     * 计算精度.
     * 计算公式: Precision =  Results intersect CorrectInDataBase / Results
     * @return
     */
    public double precision();

    /**
     * 计算准确率. 异常检测里这一个指标应该没什么意义.
     * 计算公式: Accuracy = RightResults / Results.
     * @return
     */
    @Deprecated
    public double accuracy();
}
