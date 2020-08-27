package Model.GeneratedCode.Tactics;

import Model.AbstactClass.Rule.Configuration;
import Model.AbstactClass.Rule.Tactic;
import Model.GeneratedCode.Behavior.CS;

import static Model.GeneratedCode.Behavior.CleaningSoS.maxNumOfSweepingRobotType2;
import static Model.GeneratedCode.Behavior.SoS.csModelList;
import static Model.GeneratedCode.Behavior.SoS.csSpecificationList;

public class AddSweepingRobotType2 extends Tactic implements Cloneable {
    private double remainTime;

    public AddSweepingRobotType2() {

        super(3, 2, "AddSweepingRobotType2");
        remainTime = getLatency();
    }

    @Override
    public double[] run(Configuration configuration) throws CloneNotSupportedException {
        double[] ret = new double[2]; //cost, latency

        if(configuration.getConfigurations().get("numSweepingRobotType2") + 1 >= maxNumOfSweepingRobotType2) {
            setExecuted(true);
            ret[1] = 1; //1 tick 소요됨
            return ret;
        }

        if(--remainTime == 0) {


            ret[0] = getCost();
            ret[1] = getLatency();
            int currentSweepingRobotType2 = configuration.getConfigurations().get("numSweepingRobotType2");
            csModelList.put(String.format("SweepingRobotType2_%s",currentSweepingRobotType2 + 1), (CS) csSpecificationList.get("SweepingRobotType2").clone());
            configuration.getConfigurations().put("numSweepingRobotType2", configuration.getConfigurations().get("numSweepingRobotType2") + 1);
            setExecuted(true);
        }
        return ret;
    }

}
