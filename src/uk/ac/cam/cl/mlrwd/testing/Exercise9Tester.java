package uk.ac.cam.cl.mlrwd.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise9;
import uk.ac.cam.cl.yd278.exercises.Exercise9;

public class Exercise9Tester {
	
	static final Path dataFile = Paths.get("data/bio_dataset.txt");

	public static void main(String[] args) throws IOException {

		List<List<AminoAcid>> obsSeqs = new ArrayList<List<AminoAcid>>();
		List<List<Feature>> hiddenSeqs = new ArrayList<List<Feature>>();

		try (BufferedReader reader = Files.newBufferedReader(dataFile)) {
			reader.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					if (!line.isEmpty()) {
						if (line.startsWith("#")) {
							obsSeqs.add(line.substring(1).chars()
									.mapToObj(i -> AminoAcid.valueOf((char)i))
									.collect(Collectors.toList()));
						} else {
							hiddenSeqs.add(line.chars()
									.mapToObj(i -> Feature.valueOf((char)i))
									.collect(Collectors.toList()));
						}
					}
				}
			});
		} catch (IOException e) {
			throw new IOException("Can't access the file " + dataFile, e);
		}
		
		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = IntStream.range(0, obsSeqs.size())
				.mapToObj(i -> new HMMDataStore<AminoAcid, Feature>(obsSeqs.get(i), hiddenSeqs.get(i)))
				.collect(Collectors.toList());

		// Use for testing the code		
		Collections.shuffle(sequencePairs, new Random(0));
		int testSize = sequencePairs.size()/10;
		List<HMMDataStore<AminoAcid, Feature>> devSet = sequencePairs.subList(0, testSize);
		List<HMMDataStore<AminoAcid, Feature>> testSet = sequencePairs.subList(testSize, 2*testSize);
		List<HMMDataStore<AminoAcid, Feature>> trainingSet = sequencePairs.subList(testSize*2, sequencePairs.size());
		// But:
		// TODO: Replace with cross-validation for the tick.
		
		
		IExercise9 implementation = (IExercise9) new Exercise9();

		HiddenMarkovModel<AminoAcid, Feature> model = implementation
				.estimateHMM(trainingSet);
		System.out.println("Predicted transitions:");
		System.out.println(model.getTransitionMatrix());
		System.out.println();
		System.out.println("Predicted emissions:");
		System.out.println(model.getEmissionMatrix());
		System.out.println();
		System.out.println("Predicted start probabilities:");
		System.out.println(model.getInitialProbs());
		System.out.println();

		Map<Feature, Double> finalProbs = implementation.getFinalProbs(trainingSet);
		HMMDataStore<AminoAcid, Feature> data = devSet.get(0);
		List<Feature> predicted = implementation.viterbi(model, finalProbs, data.observedSequence);
		System.out.println("True hidden sequence:");
		System.out.println(data.hiddenSequence);
		System.out.println();
		
		System.out.println("Predicted hidden sequence:");
		System.out.println(predicted);
		System.out.println();

		Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, finalProbs, devSet);
		double accuracy = implementation.precision(true2PredictedSequences);
		System.out.println("Prediction precision:");
		System.out.println(accuracy);
		System.out.println();
		
		double recall = implementation.recall(true2PredictedSequences);
		System.out.println("Prediction recall:");
		System.out.println(recall);
		System.out.println();
		
		double f1Score = implementation.fOneMeasure(true2PredictedSequences);
		System.out.println("Prediction F1 score:");
		System.out.println(f1Score);
		System.out.println();
	}
}
