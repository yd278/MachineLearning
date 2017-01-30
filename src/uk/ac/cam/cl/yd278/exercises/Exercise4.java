package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Anchor on 2017/1/30.
 */
public class Exercise4 implements IExercise4 {
    @Override
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<String, ImprovedSentiment> lexicon = Classifier.getLexicon(lexiconFile);
        Map<Path, Sentiment> result = new HashMap<>();
        int positiveCount;
        for (Path p : testSet) {
            positiveCount = 0;
            List<String> words = Tokenizer.tokenize(p);
            for (String word : words) {
                ImprovedSentiment s = lexicon.get(word);
                if (s == null) continue;
                if (s.getSentiment() == Sentiment.POSITIVE) {
                    if (s.strong()) positiveCount += 2;
                    else positiveCount++;
                }
                if (s.getSentiment() == Sentiment.NEGATIVE) {
                    if (s.strong()) positiveCount -= 2;
                    else positiveCount--;
                }
            }
            if (positiveCount >= 0) result.put(p, Sentiment.POSITIVE);
            else result.put(p, Sentiment.NEGATIVE);
        }
        return result;
    }

    private BigInteger fac(int n) {
        BigInteger bigNFact = BigInteger.ONE;
        BigInteger iterator = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            iterator = iterator.add(BigInteger.ONE);
            bigNFact = bigNFact.multiply(iterator);
        }
        return bigNFact;
    }


    private BigInteger combinatorial(int n, int k) {
        BigInteger bigNFact = fac(n);
        BigInteger bigKFact = fac(k);
        BigInteger bigLFact = fac(n - k);
        return bigNFact.divide(bigKFact).divide(bigLFact);
    }

    @Override
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
        int plus = 0;
        int minus = 0;
        int mNull = 0;
        double q = 0.5;
        for (Path p : actualSentiments.keySet()) {
            Sentiment a = classificationA.get(p);
            Sentiment b = classificationB.get(p);
            Sentiment actual = actualSentiments.get(p);
            if (a.equals(b)) mNull++;
            if (a.equals(actual) && !b.equals(actual)) plus++;
            if (!a.equals(actual) && b.equals(actual)) minus++;
        }
        double result = 0;
        int n = plus + minus + mNull+ (mNull & 1);
        int k = Math.min(plus, minus) + mNull / 2 + (mNull & 1);
        for (int i = 0; i <= k; i++) {
            double c = combinatorial(n, i).doubleValue();
            c = c * Math.pow(q, i) * Math.pow(1 - q, n - i);
            result += 2 * c;

        }
        return result;
    }
}
