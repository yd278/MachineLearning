package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anchor on 2017/2/13.
 */
public class Exercise7 implements IExercise7 {
    @Override

    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
        //initialize the matrices
        Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        for (DiceType from : DiceType.values()) {
            Map<DiceType, Double> tmp = new HashMap<>();
            for (DiceType to : DiceType.values()) {
                tmp.put(to, 0.0);
            }
            transitionMatrix.put(from, tmp);
        }
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();
        for (DiceType from : DiceType.values()) {
            Map<DiceRoll, Double> tmp = new HashMap<>();
            for (DiceRoll to : DiceRoll.values()) {
                tmp.put(to, 0.0);
            }
            emissionMatrix.put(from, tmp);
        }
        Map<DiceType, Double> initProbs = new HashMap<>();
        for (DiceType diceType : DiceType.values()) {
            initProbs.put(diceType, 0.0);
        }
        List<HMMDataStore<DiceRoll, DiceType>> dataStores = HMMDataStore.loadDiceFiles(sequenceFiles);
        for (HMMDataStore<DiceRoll, DiceType> dataStore : dataStores) {
            List<DiceRoll> diceRolls = dataStore.observedSequence;
            List<DiceType> diceTypes = dataStore.hiddenSequence;
            //count Type
            DiceType initDiceType = diceTypes.get(0);
            double d = initProbs.get(initDiceType);
            initProbs.put(initDiceType, d + 1);
            //count Emission
            for (int i = 0; i < diceRolls.size(); i++) {
                DiceRoll diceRoll = diceRolls.get(i);
                DiceType diceType = diceTypes.get(i);
                double f = emissionMatrix.get(diceType).get(diceRoll);
                emissionMatrix.get(diceType).put(diceRoll, f + 1);
            }

            //count transition
            DiceType previousDiceType = diceTypes.get(0);
            for (int i = 1; i < diceTypes.size(); i++) {
                DiceType currentDiceType = diceTypes.get(i);
                double f = transitionMatrix.get(previousDiceType).get(currentDiceType);
                transitionMatrix.get(previousDiceType).put(currentDiceType, f + 1);
                previousDiceType = currentDiceType;
            }
        }

        return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix, initProbs);
    }
}
