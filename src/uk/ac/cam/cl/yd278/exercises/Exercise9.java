package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anchor on 2017/2/20.
 */
public class Exercise9 implements IExercise9 {
    @Override
    public Map<Feature, Double> getFinalProbs(List<HMMDataStore<AminoAcid, Feature>> trainingPairs) throws IOException {
        Map<Feature, Double> result = new HashMap<>();
        for (Feature feature : Feature.values()) {
            result.put(feature, 0.0);
        }
        double total = 0;
        for (HMMDataStore<AminoAcid, Feature> dataStore : trainingPairs) {
            List<Feature> hiddenSequence = dataStore.hiddenSequence;
            Feature end = hiddenSequence.get(hiddenSequence.size() - 1);
            double f = result.get(end);
            result.put(end, f + 1);
        }
        return result;
    }

    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {

        Map<Feature, Map<Feature, Double>> transitionMatrix = new HashMap<>();
        return null;
    }

    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, Map<Feature, Double> finalProbs, List<AminoAcid> observedSequence) {
        return null;
    }

    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, Map<Feature, Double> finalProbs, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        return null;
    }

    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        return 0;
    }

    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        return 0;
    }

    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        return 0;
    }
}
