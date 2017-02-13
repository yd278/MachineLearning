package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Anchor on 2017/2/6.
 */
public class Exercise6 implements IExercise6 {
    private Map<NuancedSentiment, Double> plusOne(Map<NuancedSentiment, Double> origin, NuancedSentiment sentiment) {
        if (origin.containsKey(sentiment)) {
            Double f = origin.get(sentiment) + 1;
            origin.put(sentiment, f);
        } else {
            Double f = 1d;
            origin.put(sentiment, f);
        }
        return origin;
    }

    @Override
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        int total = 0;
        Map<NuancedSentiment, Double> result = new HashMap<>();
        for (Map.Entry<Path, NuancedSentiment> entry : trainingSet.entrySet()) {
            NuancedSentiment s = entry.getValue();
            result = plusOne(result, s);
            total++;
        }
        for (NuancedSentiment s : result.keySet()) {
            Double f = result.get(s) / total;
            result.put(s, f);
        }
        return result;
    }

    private Map<String, Map<NuancedSentiment, Double>> countWords(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<String, Map<NuancedSentiment, Double>> result = new HashMap<>();
        for (Path p : trainingSet.keySet()) {
            List<String> tokens = Tokenizer.tokenize(p);
            NuancedSentiment sentiment = trainingSet.get(p);
            for (String word : tokens) {
                if (result.containsKey(word)) {
                    Map<NuancedSentiment, Double> numOfWOrd = result.get(word);
                    numOfWOrd = plusOne(numOfWOrd, sentiment);
                    result.put(word, numOfWOrd);
                } else {

                    Map<NuancedSentiment, Double> numOfWOrd = new HashMap<>();
                    numOfWOrd = plusOne(numOfWOrd, sentiment);
                    result.put(word, numOfWOrd);
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<String, Map<NuancedSentiment, Double>> result = new HashMap<>();
        Map<String, Map<NuancedSentiment, Double>> count = countWords(trainingSet);

        Map<NuancedSentiment, Double> total = new HashMap<>();
        for (String word : count.keySet()) {
            Map<NuancedSentiment, Double> numOfWord = count.get(word);
            for (NuancedSentiment sentiment : NuancedSentiment.values()) {
                total = plusOne(total, sentiment);
                Double f = total.get(sentiment) + ((numOfWord.get(sentiment) == null) ? 0 : numOfWord.get(sentiment));
                total.put(sentiment, f);
            }
        }


        for (String word : count.keySet()) {
            Map<NuancedSentiment, Double> logProbs = new HashMap<>();
            for (NuancedSentiment sentiment : NuancedSentiment.values()) {
                double cnt = (count.get(word).get(sentiment) == null) ? 0 : count.get(word).get(sentiment);

                Double logProb = Math.log((cnt + 1) / total.get(sentiment));
                logProbs.put(sentiment, logProb);
            }
            result.put(word, logProbs);
        }
        return result;
    }

    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
        Map<Path, NuancedSentiment> result = new HashMap<>();
        for (Path p : testSet) {
            double max = Double.NEGATIVE_INFINITY;
            NuancedSentiment cNB = null;
            List<String> tokens = Tokenizer.tokenize(p);
            for (NuancedSentiment sentiment : NuancedSentiment.values()) {
                double sum = Math.log(classProbabilities.get(sentiment));
                for (String word : tokens) {
                    Map<NuancedSentiment, Double> probs = tokenLogProbs.get(word);
                    if (probs != null) {
                        sum += probs.get(sentiment);
                    } else {
                        sum += Math.log(0.5);
                    }
                }
                if (sum >= max) {
                    max = sum;
                    cNB = sentiment;
                }
            }
            result.put(p, cNB);
        }
        return result;
    }

    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
        double total = trueSentiments.size();
        double correct = 0;
        for (Path p : trueSentiments.keySet()) {
            if (trueSentiments.get(p) == predictedSentiments.get(p)) {
                correct++;
            }
        }
        return correct / total;
    }

    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {
        Map<Integer, Map<Sentiment, Integer>> result = new HashMap<>();
        for (Map<Integer, Sentiment> predictedSentiment : predictedSentiments) {
            for (Integer idx : predictedSentiment.keySet()) {
                Sentiment sentiment = predictedSentiment.get(idx);
                Map<Sentiment, Integer> counts;
                if (result.containsKey(idx)) {
                    counts = result.get(idx);
                } else {
                    counts = new HashMap<>();
                }
                Integer count;
                if (counts.containsKey(sentiment)) count = counts.get(sentiment) + 1;
                else count = 1;
                counts.put(sentiment, count);
                result.put(idx, counts);
            }
        }
        return result;
    }

    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
        Map<Integer, Integer> n = new HashMap<>();
        for (Integer idx : agreementTable.keySet()) {
            int count = 0;
            for (Sentiment sentiment : Sentiment.values()) {
                Map<Sentiment, Integer> tmp = agreementTable.get(idx);
                if (tmp.containsKey(sentiment)) count += tmp.get(sentiment);
            }
            n.put(idx, count);
        }
        double N = agreementTable.size();
        double Pa = 0;
        for (Integer i : agreementTable.keySet()) {
            double ni = n.get(i);
            double para = 1 / ni / (ni - 1);
            double sum = 0;
            for (Sentiment j : Sentiment.values()) {
                if (agreementTable.get(i).containsKey(j)) {
                    double nij = agreementTable.get(i).get(j);
                    sum += nij * (nij - 1);
                }
            }
            Pa += sum * para;
        }
        Pa /= N;
        double Pe = 0;
        for (Sentiment j : Sentiment.values()) {
            double sum = 0;
            for (Integer i : agreementTable.keySet()) {
                if (agreementTable.get(i).containsKey(j)) {
                    sum += (double) agreementTable.get(i).get(j) / (double) n.get(i);
                }
            }
            sum /= N;
            Pe += sum * sum;
        }
        return (Pa - Pe) / (1 - Pe);

    }
}
