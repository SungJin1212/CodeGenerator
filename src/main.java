import AbstactClass.SoS;
import GeneratedCode.cleaningSoS;
import Simulator.Simulator;

public class main {
    public static void main(String[] args) {
        SoS cleaningSoS = new cleaningSoS();

        Simulator simulator = new Simulator(cleaningSoS);

        simulator.StartSimulation();


    }
}
