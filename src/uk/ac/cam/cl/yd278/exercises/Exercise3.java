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
    int total = 0;
    private Map<String, Double> count = new HashMap<>();

    public static void main(String args[]) throws IOException {
        Exercise3 e = new Exercise3();
        Path reviewsDir = Paths.get("large_dataset");
        e.plotFrequencyVsRank(reviewsDir);

    }
    public void sort(List<Map.Entry<String, Double>> list){

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                double diff = o2.getValue() - o1.getValue();
                if(diff > 0) return 1;
                if(diff < 0) return -1;
                return 0;
            }
        });
    }
    public List<Map.Entry<String, Double>> calculateSortedFrequencies(Path reviewsDir) throws IOException {
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
        for (String s : count.keySet()) {
            Double f = count.get(s) / (double) total;
            count.put(s, f);
        }

        List<Map.Entry<String, Double>> list =
                new ArrayList<Map.Entry<String, Double>>(count.entrySet());
        sort(list);
        return list;

    }

    public void plotFrequencyVsRank(Path reviewsDir) throws IOException {
        List<Map.Entry<String, Double>> frequencies = calculateSortedFrequencies(reviewsDir);
        List<BestFit.Point> points = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            BestFit.Point p = new BestFit.Point((double) i + 1, frequencies.get(i).getValue());
            points.add(p);
        }
        ChartPlotter.plotLines(points);
        //BONUS: plot 10 words selected in task 1
        Map<String,Double> tenWords = new HashMap<>();
        tenWords.put("best",count.get("best"));
        tenWords.put("well",count.get("well"));
        tenWords.put("satisfying",count.get("satisfying"));
        tenWords.put("awesome",count.get("awesome"));
        tenWords.put("mistaken",count.get("mistaken"));
        tenWords.put("bland",count.get("bland"));
        tenWords.put("lacking",count.get("lacking"));
        tenWords.put("unfortunately",count.get("unfortunately"));
        tenWords.put("relax",count.get("relax"));
        tenWords.put("dramatic",count.get("dramatic"));
        List<Map.Entry<String, Double>> sortedTenWords =
                new ArrayList<>(count.entrySet());
        



        //然而题意理解错了
        //把10个词都放进map先 然后On扫一遍整个frequency
        //并不想写
        //回去再说
    }

}
