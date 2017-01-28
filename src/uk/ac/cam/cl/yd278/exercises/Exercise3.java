package uk.ac.cam.cl.yd278.exercises;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrwd.utils.BestFit;
import uk.ac.cam.cl.mlrwd.utils.ChartPlotter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Anchor on 2017/1/27.
 */
public class Exercise3 {
    private int total = 0;
    private Map<String, Double> count = new HashMap<>();
    private Set<String> tenWords = new HashSet<>();
    public static void main(String args[]) throws IOException {
        Exercise3 e = new Exercise3();
        Path reviewsDir = Paths.get("large_dataset");
        e.plotFrequencyVsRank(reviewsDir);

    }
    public void sort(List<Map.Entry<String, Double>> list){

        list.sort((o1, o2) -> {
            double diff = o2.getValue() - o1.getValue();
            if (diff > 0) return 1;
            if (diff < 0) return -1;
            return 0;
        });
    }
    private List<Map.Entry<String, Double>> calculateSortedFrequencies(Path reviewsDir) throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(reviewsDir)) {
            for (Path item : files) {
                List<String> tokens = Tokenizer.tokenize(item);
                for (String s : tokens) {
                    Double c = count.get(s);
                    if (c != null) {
                        c = c + 1.0;
                        count.put(s, c);
                    } else {
                        c = 1.0;
                        count.put(s, c);
                    }
                }
                total += tokens.size();
            }

        } catch (IOException e) {
            throw new IOException("Can't read the reviews.", e);
        }

        System.out.println("calculating...");
        for (String s : count.keySet()) {
            Double f = count.get(s) / (double) total;
            count.put(s, f);
        }

        List<Map.Entry<String, Double>> list =
                new ArrayList<>(count.entrySet());
        sort(list);
        return list;

    }
    private boolean inTenWords(String s){
        return tenWords.contains(s);
    }
    private void plotFrequencyVsRank(Path reviewsDir) throws IOException {
        System.out.println("reading data...");
        List<Map.Entry<String, Double>> frequencies = calculateSortedFrequencies(reviewsDir);
        List<BestFit.Point> points = new LinkedList<>();
        List<BestFit.Point> tenPoints = new LinkedList<>();
        tenWords.add("best");
        tenWords.add("well");
        tenWords.add("satisfying");
        tenWords.add("awesome");
        tenWords.add("mistaken");
        tenWords.add("bland");
        tenWords.add("lacking");
        tenWords.add("unfortunately");
        tenWords.add("relax");
        tenWords.add("dramatic");
        System.out.println("plotting...");
        for (int i = 0; i < 10000; i++) {
            if(i%1000==0){

                System.out.println(i/100 + "%");
            }
            BestFit.Point p = new BestFit.Point((double) i + 1, frequencies.get(i).getValue());
            if(inTenWords(frequencies.get(i).getKey()))tenPoints.add(p);
            points.add(p);
        }
        ChartPlotter.plotLines(points);
        ChartPlotter.plotLines(tenPoints);
    }

}
