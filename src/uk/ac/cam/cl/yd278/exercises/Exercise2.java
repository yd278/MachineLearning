package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Anchor on 2017/1/23.
 */
public class Exercise2 implements IExercise2 {
    @Override
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {

        int positiveCount = 0;
        int negativeCount = 0;
        for (Path p : trainingSet.keySet()) {
            if (trainingSet.get(p) == Sentiment.POSITIVE) positiveCount++;
            if (trainingSet.get(p) == Sentiment.NEGATIVE) negativeCount++;
        }
        int total = positiveCount + negativeCount;
        Map<Sentiment, Double> result = new HashMap<>();
        result.put(Sentiment.POSITIVE, (double) positiveCount / (double) total);
        result.put(Sentiment.NEGATIVE, (double) negativeCount / (double) total);
        return result;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Double>> result = new HashMap<>();
        Map<String, Word> words = countWords(trainingSet);
        int totalPositiveCount = 0;
        int totalNegativeCount = 0;
        for (String str : words.keySet()) {
            totalPositiveCount += words.get(str).getPositiveCount();
            totalNegativeCount += words.get(str).getNegativeCount();
        }
        for (String str : words.keySet()) {
            Map<Sentiment, Double> prob = new HashMap<>();
            double positiveCount = (double) words.get(str).getPositiveCount();
            double negativeCount = (double) words.get(str).getNegativeCount();
            Double posiProb = Math.log(positiveCount / (double) totalPositiveCount);
            Double negaProb = Math.log(negativeCount / (double) totalNegativeCount);
            prob.put(Sentiment.POSITIVE, posiProb);
            prob.put(Sentiment.NEGATIVE, negaProb);
            result.put(str, prob);
        }
        return result;
    }

    private Map<String, Word> countWords(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Word> words = new HashMap<>();
        for (Path p : trainingSet.keySet()) {
            Sentiment s = trainingSet.get(p);
            List<String> tokens = Tokenizer.tokenize(p);
            for (String str : tokens) {
                Word w = words.get(str);
                if (w != null) {
                    if (s == Sentiment.POSITIVE) w.plusOnePositive();
                    if (s == Sentiment.NEGATIVE) w.plusOneNegative();
                } else {
                    w = new Word();
                    if (s == Sentiment.POSITIVE) w.plusOnePositive();
                    if (s == Sentiment.NEGATIVE) w.plusOneNegative();
                    words.put(str, w);
                }
            }
        }
        return words;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Map<Sentiment, Double>> result = new HashMap<>();
        Map<String, Word> words = countWords(trainingSet);
        int totalPositiveCount = 0;
        int totalNegativeCount = 0;

        for (String str : words.keySet()) {
            totalPositiveCount += words.get(str).getPositiveCount();
            totalPositiveCount++;
            totalNegativeCount += words.get(str).getNegativeCount();
            totalNegativeCount++;
        }
        for (String str : words.keySet()) {
            Map<Sentiment, Double> prob = new HashMap<>();
            double positiveCount = (double) words.get(str).getPositiveCount();
            double negativeCount = (double) words.get(str).getNegativeCount();
            Double posiProb = Math.log((positiveCount + 1) / totalPositiveCount);
            Double negaProb = Math.log((negativeCount + 1) / totalNegativeCount);
            prob.put(Sentiment.POSITIVE, posiProb);
            prob.put(Sentiment.NEGATIVE, negaProb);
            result.put(str, prob);
        }
        return result;
    }

    @Override
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
        Map<Path, Sentiment> result = new HashMap<>();
        for (Path path : testSet) {
            double max = Double.NEGATIVE_INFINITY;
            Sentiment cNB = null;
            List<String> tokens = Tokenizer.tokenize(path);
            for (Sentiment s : classProbabilities.keySet()) {
                double sum = Math.log(classProbabilities.get(s));
                for (String str : tokens) {
                    Map<Sentiment, Double> probs = tokenLogProbs.get(str);
                    if (probs != null) {
                        sum += probs.get(s);
                    } else {
                        sum += Math.log(0.5);
                    }
                }

                if (sum >= max) {
                    max = sum;
                    cNB = s;
                }

            }
            result.put(path, cNB);
        }
        return result;
    }
}
