package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Anchor on 2017/2/17.
 */
public class Exercise8 implements IExercise8 {

    @Override
    public Map<DiceType, Double> getFinalProbs(List<Path> trainingFiles) throws IOException {
        List<HMMDataStore<DiceRoll, DiceType>> dataStores = HMMDataStore.loadDiceFiles(trainingFiles);
        Map<DiceType, Double> finalProbs = new HashMap<>();
        for (DiceType diceType : DiceType.values()) {
            finalProbs.put(diceType, 0.0);
        }
        for (HMMDataStore<DiceRoll, DiceType> dataStore : dataStores) {
            List<DiceType> diceTypes = dataStore.hiddenSequence;
            DiceType last = diceTypes.get(diceTypes.size() - 1);
            double f = finalProbs.get(last);
            finalProbs.put(last, f + 1);
        }
        int total = dataStores.size();
        for (DiceType diceType : DiceType.values()) {
            double f = finalProbs.get(diceType);
            finalProbs.put(diceType, f / total);
        }

        return finalProbs;
    }

    @Override
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, Map<DiceType, Double> finalProbs, List<DiceRoll> observedSequence) {
        int T = observedSequence.size(); //observation [0,T)
        List<Map<DiceType, Double>> logProbs = new ArrayList<>();  //[0,T)
        List<Map<DiceType, DiceType>> records = new ArrayList<>(); //[0, T-1)

        //init
        Map<DiceType, Double> initProb = new HashMap<>();
        DiceRoll firstObservation = observedSequence.get(0);
        for (DiceType diceType : DiceType.values()) {
            double prob = Math.log(model.getInitialProbs().get(diceType));
            prob += Math.log(model.getPossibleEmissions(diceType).get(firstObservation));
            initProb.put(diceType, prob);
        }
        logProbs.add(initProb);

        //main
        for (int i = 1; i < T; i++) {
            DiceRoll observation = observedSequence.get(i);
            Map<DiceType, Double> prob = new HashMap<>();
            Map<DiceType, DiceType> record = new HashMap<>();
            for (DiceType diceType : DiceType.values()) {
                double max = Double.NEGATIVE_INFINITY;
                for (DiceType from : DiceType.values()) {
                    double f = logProbs.get(i - 1).get(from);
                    f += Math.log(model.getPossibleTransitions(from).get(diceType));
                    f += Math.log(model.getPossibleEmissions(diceType).get(observation));
                    if (f > max) {
                        max = f;
                        record.put(diceType, from);
                    }
                }
                prob.put(diceType, max);
            }
            logProbs.add(prob);
            records.add(record);
        }
        //final state
        double maxFinalProb = Double.NEGATIVE_INFINITY;
        DiceType finalDiceType = null;
        Map<DiceType, Double> finalProb = logProbs.get(T - 1);
        for (DiceType finalType : DiceType.values()) {
            double f = finalProb.get(finalType);
            f += Math.log(finalProbs.get(finalType));
            if (f > maxFinalProb) {
                maxFinalProb = f;
                finalDiceType = finalType;
            }
        }
        List<DiceType> result = new ArrayList<>();
        result.add(finalDiceType);
        DiceType currentDiceType = finalDiceType;
        for (int i = T - 2; i >= 0; i--) {
            result.add(records.get(i).get(currentDiceType));
            currentDiceType = records.get(i).get(currentDiceType);
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, Map<DiceType, Double> finalProbs, List<Path> testFiles) throws IOException {
        Map<List<DiceType>, List<DiceType>> results = new HashMap<>();
        List<HMMDataStore<DiceRoll, DiceType>> dataStores = HMMDataStore.loadDiceFiles(testFiles);
        for (HMMDataStore dataStore : dataStores) {
            List<DiceType> result = viterbi(model, finalProbs, dataStore.observedSequence);
            results.put(dataStore.hiddenSequence, result);
        }
        return results;
    }


    @Override
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double total = 0;
        double correct = 0;
        for (Map.Entry<List<DiceType>, List<DiceType>> entry : true2PredictedMap.entrySet()) {
            for (int i = 0; i < entry.getKey().size(); i++) {
                if (entry.getValue().get(i) == DiceType.WEIGHTED) {
                    total += 1;
                    if (entry.getKey().get(i) == DiceType.WEIGHTED) {
                        correct += 1;
                    }
                }
            }
        }
        return correct / total;
    }

    @Override
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double total = 0;
        double correct = 0;
        for (Map.Entry<List<DiceType>, List<DiceType>> entry : true2PredictedMap.entrySet()) {
            for (int i = 0; i < entry.getKey().size(); i++) {
                if (entry.getKey().get(i) == DiceType.WEIGHTED) {
                    total += 1;
                    if (entry.getValue().get(i) == DiceType.WEIGHTED) {
                        correct += 1;
                    }
                }
            }
        }
        return correct / total;
    }

    @Override
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);
        return 2 * precision * recall / (precision + recall);
    }
}
