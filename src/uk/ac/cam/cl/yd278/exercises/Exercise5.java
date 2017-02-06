package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.*;

/**
 * Created by Anchor on 2017/2/3.
 */
public class Exercise5 implements IExercise5 {

    /*
    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path, Sentiment>> result = new LinkedList<>();
        Random random = new Random(seed);
        List<Map.Entry<Path, Sentiment>> entries = new ArrayList<>(dataSet.entrySet());
        int total = entries.size();
        boolean used[] = new boolean[total];
        int each = total / 10; //given that 10 | total
        for (int foldIdx = 0; foldIdx < 10; foldIdx++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int count = 0; count < each; count++) {
                int idx;
                do {
                    idx = random.nextInt(total);
                } while (used[idx]);
                used[idx] = true;
                fold.put(entries.get(idx).getKey(), entries.get(idx).getValue());
            }
            result.add(fold);
        }
        return result;
    }
    */

    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path, Sentiment>> result = new LinkedList<>();
        List<Map.Entry<Path, Sentiment>> entries = new ArrayList<>(dataSet.entrySet());
        int total = entries.size();
        int each = total / 10; //given that 10 | total
        Collections.shuffle(entries, new Random(seed));
        for (int foldIdx = 0; foldIdx < 10; foldIdx++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int count = 0; count < each; count++) {
                int posi = each * foldIdx + count;
                fold.put(entries.get(posi).getKey(), entries.get(posi).getValue());
            }
            result.add(fold);
        }
        return result;
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path, Sentiment>> result = new LinkedList<>();
        List<Map.Entry<Path, Sentiment>> entries = new ArrayList<>(dataSet.entrySet());
        Collections.shuffle(entries,new Random(seed));
        List<Map.Entry<Path, Sentiment>> positiveList = new ArrayList<>();
        List<Map.Entry<Path, Sentiment>> negativeList = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getValue() == Sentiment.POSITIVE) positiveList.add(entries.get(i));
            if (entries.get(i).getValue() == Sentiment.NEGATIVE) negativeList.add(entries.get(i));
        }
        int total = dataSet.size();
        int each = total / 10 / 2; //given that dataset is balanced
        for (int foldIdx = 0; foldIdx < 10; foldIdx++) {
            Map<Path, Sentiment> fold = new HashMap<>();
            for (int count = 0; count < each; count++) {
                int posi = each * foldIdx + count;
                fold.put(positiveList.get(posi).getKey(),Sentiment.POSITIVE);
                fold.put(negativeList.get(posi).getKey(),Sentiment.NEGATIVE);
            }
            result.add(fold);
        }
        return result;
    }
/*
    public Map<Path,Sentiment> lexiconResult(Map<Path,Sentiment> trainingSet) throws IOException {
        Map<Path,Sentiment> result = new HashMap<>();
        Exercise1 implement = new Exercise1();

        Path lexiconFile = Paths.get("data/sentiment_lexicon");
        result = implement.simpleClassifier(trainingSet.keySet(),lexiconFile);
        return result;
    }*/
    @Override
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
        double results[] = new double[10];
        IExercise2 implement = new Exercise2();
        for (int test = 9; test >= 0; test--) {
            //data preparation
            Map<Path, Sentiment> testSet = folds.get(test);
            Map<Path, Sentiment> trainingSet = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                if (i != test) {
                    trainingSet.putAll(folds.get(i));
                }
            }
            //training
            Map<Sentiment, Double> classProbabilities = implement.calculateClassProbabilities(trainingSet);
            Map<String, Map<Sentiment, Double>> smoothedLogProbs = implement.calculateSmoothedLogProbs(trainingSet);

            //testing
            Map<Path, Sentiment> predictedResult = implement.naiveBayes(testSet.keySet(), smoothedLogProbs, classProbabilities);
            //Map<Path, Sentiment> predictedResult = lexiconResult(testSet);
            int total = testSet.size();
            int correct = 0;
            for (Path p : testSet.keySet()) {
                if (predictedResult.get(p) == testSet.get(p)) correct++;
            }
            results[test] = (double) correct / (double) total;


        }
        return results;
    }

    @Override
    public double cvAccuracy(double[] scores) {
        double sum = 0;
        for (double score : scores) sum += score;
        return sum / scores.length;
    }

    @Override
    public double cvVariance(double[] scores) {
        double mean = cvAccuracy(scores);
        int n = scores.length;
        double sum = 0;
        for (double score : scores) {
            sum += (score - mean) * (score - mean);
        }
        return sum / n;
    }
    public static void main(String args[]){

    }
}
