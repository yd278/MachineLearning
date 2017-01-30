package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anchor on 2017/1/30.
 */
public class Classifier {
    public static Map<String, ImprovedSentiment> getLexicon(Path lexiconFile) throws IOException {
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
}
