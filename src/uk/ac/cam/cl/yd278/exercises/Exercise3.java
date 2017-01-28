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
        e.deal(reviewsDir);

    }
    public void sort(List<Map.Entry<String, Double>> list){

        list.sort((o1, o2) -> {
            double diff = o2.getValue() - o1.getValue();
            if (diff > 0) return 1;
            if (diff < 0) return -1;
            return 0;
        });
    }
    public boolean isPowerOfTwo(int n) {
        if(n<=0) return false;
        n = (n & 0x55555555) + ((n >> 1) & 0x55555555);
        n = (n & 0x33333333) + ((n >> 2) & 0x33333333);
        n = (n & 0x0f0f0f0f) + ((n >> 4) & 0x0f0f0f0f);
        n = (n & 0x00ff00ff) + ((n >> 8) & 0x00ff00ff);
        n = (n & 0x0000ffff) + ((n >> 16) & 0x0000ffff);

        return (n == 1);

    }
    private List<Map.Entry<String, Double>> readData(Path reviewsDir) throws IOException {
        List<BestFit.Point> tokensVsTypes = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(reviewsDir)) {
            int cnt = 0;
            for (Path item : files) {
                List<String> tokens = Tokenizer.tokenize(item);
                for (String s : tokens) {
                    cnt++;
                    Double c = count.get(s);
                    if (c != null) {
                        c = c + 1.0;
                        count.put(s, c);
                    } else {
                        c = 1.0;
                        count.put(s, c);
                    }
                    if(isPowerOfTwo(cnt)){
                        tokensVsTypes.add(new BestFit.Point(cnt,count.size()));
                    }
                }
                total += tokens.size();

            }

            tokensVsTypes.add(new BestFit.Point(cnt,count.size()));
            ChartPlotter.plotLines(tokensVsTypes);
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
    private void deal(Path reviewsDir) throws IOException { //is that really OOP....?
        System.out.println("reading data...");
        List<Map.Entry<String, Double>> frequencies = readData(reviewsDir);
        List<BestFit.Point> points = new ArrayList<>();
        List<BestFit.Point> tenPoints = new ArrayList<>();
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
            BestFit.Point p = new BestFit.Point((double) i + 1, frequencies.get(i).getValue());
            if(inTenWords(frequencies.get(i).getKey())){
                System.out.println("word: " + frequencies.get(i).getKey() + " rank" + (i+1) +" real frequency: " + frequencies.get(i).getValue()) ;
                tenPoints.add(p);
            }
            points.add(p);
        }
        ChartPlotter.plotLines(points);//frequency vs rank
        ChartPlotter.plotLines(tenPoints);//ten words selected in task 1
        List<BestFit.Point> logLogPoints = new ArrayList<>();
        for(int i = 0; i < 10000; i++){
            BestFit.Point p = points.get(i);
            logLogPoints.add(new BestFit.Point(Math.log(p.x), Math.log(p.y)));
        }
        //===================fitting

        System.out.println("fitting.....");
        Map<BestFit.Point,Double> series = new HashMap<>();
        for(int i = 0; i < 10000; i++){
            BestFit.Point p = logLogPoints.get(i);
            Double weight = points.get(i).y;
            series.put(p,weight);
        }
        BestFit.Line bestFitLine = BestFit.leastSquares(series);
        List<BestFit.Point> bestFitLinePoints = new LinkedList<>();
        double x,y;
        x = logLogPoints.get(0).x;
        y = bestFitLine.gradient * x + bestFitLine.yIntercept;
        bestFitLinePoints.add(new BestFit.Point(x,y));
        x = logLogPoints.get(9999).x;
        y = bestFitLine.gradient * x + bestFitLine.yIntercept;
        bestFitLinePoints.add(new BestFit.Point(x,y));
        ChartPlotter.plotLines(logLogPoints,bestFitLinePoints);
        System.out.println("gradient = "+ bestFitLine.gradient + " yIntercept = " + bestFitLine.yIntercept);

    }

}
