package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anchor on 2017/1/20.
 */
public class Exercise1 implements IExercise1 {

    static final Path dataDirectory = Paths.get("data/sentiment_dataset");


    private boolean isStrong(String s) {
        if (s.charAt(5) == 'w') return false;
        else return true;
    }

    public Map<String, ImprovedSentiment> getLexicon(Path lexiconFile) throws IOException {
        Map<String, ImprovedSentiment> lexicon = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(lexiconFile)) {
            reader.lines().forEach(new Consumer<String>() {
                @Override
                public void accept(String line) {
                    // Ignore neutral polarity entries.
                    Sentiment polarity;
                    boolean strong;
                    if (line.contains("priorpolarity=negative")) {
                        polarity = Sentiment.NEGATIVE;
                    } else if (line.contains("priorpolarity=positive")) {
                        polarity = Sentiment.POSITIVE;
                    } else {
                        return;
                    }
                    if (line.contains("type=strongsubj")) {
                        strong = true;
                    } else if (line.contains("type=weaksubj")) {
                        strong = false;
                    } else {
                        return;
                    }
                    Pattern p = Pattern.compile("word1=([-\\w]+) "); //some words have hyphens
                    Matcher m = p.matcher(line);
                    m.find();
                    String word = m.group(1); // If match not found, bad lexicon
                    lexicon.put(word, new ImprovedSentiment(strong, polarity));
                }
            });
        } catch (IOException e) {
            throw new IOException("Lexicon file can't be accessed.", e);
        }
        return lexicon;
    }

    @Override
    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<String, ImprovedSentiment> lexicon = getLexicon(lexiconFile);
        Map<Path, Sentiment> result = new HashMap<>();
        int positiveCount;
        for (Path p : testSet) {
            positiveCount = 0;
            List<String> words = Tokenizer.tokenize(p);
            for (String word : words) {
                ImprovedSentiment s = lexicon.get(word);
                if (s == null) continue;
                if (s.getSentiment() == Sentiment.POSITIVE) positiveCount++;
                if (s.getSentiment() == Sentiment.NEGATIVE) positiveCount--;
            }
            if (positiveCount >= 0) result.put(p, Sentiment.POSITIVE);
            else result.put(p, Sentiment.NEGATIVE);
        }
        return result;
    }

    @Override
    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
        Set<Path> paths = trueSentiments.keySet();
        int total = paths.size();
        int correct = 0;
        for (Path path : paths) {
            Sentiment trueSentient = trueSentiments.get(path);
            Sentiment predictedSentiment = predictedSentiments.get(path);
            if (trueSentient == predictedSentiment) correct++;
        }
        return (double) correct / (double) total;
    }

    @Override
    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        Map<String, ImprovedSentiment> lexicon = getLexicon(lexiconFile);
        Map<Path, Sentiment> result = new HashMap<>();
        double positivity;
        for (Path p : testSet) {
            positivity = 0;
            List<String> words = Tokenizer.tokenize(p);
            for (String word : words) {
                ImprovedSentiment s = lexicon.get(word);
                if ((s == null) && (s.getSentiment() == null)) continue;
                positivity += calculatePositivity(s,0.25);
            }
            if (positivity >= 0.0) result.put(p, Sentiment.POSITIVE);
            else result.put(p, Sentiment.NEGATIVE);
        }
        return result;

    }

    private double calculatePositivity(ImprovedSentiment s, double rate) {
        double mark = 1.0;
        if (s.getSentiment() == Sentiment.NEGATIVE) mark *= -1;
        if (!s.strong()) mark *= rate;
        return mark;
    }


}
