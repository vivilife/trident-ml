package storm.trident.ml.testing.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import storm.trident.ml.Instance;

public class Datasets {

	private final static File USPS_FILE = new File("src/test/resources/usps.csv");
	private final static File SPAM_FILE = new File("src/test/resources/spam.csv");
	private final static File BIRTHS_FILE = new File("src/test/resources/births.csv");
	private final static File REUTEURS_FILE = new File("src/test/resources/reuters.csv");

	public final static List<Instance<Boolean>> SPAM_SAMPLES = new ArrayList<Instance<Boolean>>();
	public final static List<Instance<Integer>> USPS_SAMPLES = new ArrayList<Instance<Integer>>();
	public final static List<Instance<Double>> BIRTHS_SAMPLES = new ArrayList<Instance<Double>>();
	public final static Map<Integer, List<String>> REUTERS_TRAIN_DATA = new HashMap<Integer, List<String>>();
	public final static Map<Integer, List<String>> REUTERS_EVAL_DATA = new HashMap<Integer, List<String>>();

	static {
		try {
			loadUSPSData();
			loadSPAMData();
			loadBirthsData();
			loadReutersData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadUSPSData() throws IOException {
		FileInputStream is = new FileInputStream(USPS_FILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			String line;
			while ((line = br.readLine()) != null) {
				try {
					String[] values = line.split(" ");

					Integer label = Integer.parseInt(values[0]) - 1;
					double[] features = new double[values.length - 1];
					for (int i = 1; i < values.length; i++) {
						features[i - 1] = Double.parseDouble(values[i].split(":")[1]);
					}

					USPS_SAMPLES.add(new Instance<Integer>(label, features));
				} catch (Exception ex) {
					System.out.println("Skipped USPS sample : " + line);
				}
			}

			Collections.shuffle(USPS_SAMPLES);
		} finally {
			is.close();
			br.close();
		}
	}

	private static void loadSPAMData() throws IOException {
		FileInputStream is = new FileInputStream(SPAM_FILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			String line;
			while ((line = br.readLine()) != null) {
				try {
					String[] values = line.split(";");

					Boolean label = "1".equals(values[values.length - 1]);
					double[] features = new double[values.length - 1];
					for (int i = 0; i < values.length - 1; i++) {
						double original = Double.parseDouble(values[i]);
						double rescaled = -3.0 + 4.0 / (1 + Math.exp(-(original)));
						features[i] = rescaled;
					}

					SPAM_SAMPLES.add(new Instance<Boolean>(label, features));
				} catch (Exception ex) {
					System.out.println("Skipped PML sample : " + line);
				}
			}

			Collections.shuffle(SPAM_SAMPLES);
		} finally {
			is.close();
			br.close();
		}
	}

	private static void loadBirthsData() throws IOException {
		FileInputStream is = new FileInputStream(BIRTHS_FILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		try {
			String line;
			while ((line = br.readLine()) != null) {
				try {
					String[] values = line.split(";");

					Double label = Double.parseDouble(values[values.length - 1]);
					double[] features = new double[values.length - 1];
					for (int i = 1; i < values.length - 1; i++) {
						features[i - 1] = Double.parseDouble(values[i]);
					}

					BIRTHS_SAMPLES.add(new Instance<Double>(label, features));
				} catch (Exception ex) {
					System.out.println("Skipped PML sample : " + line);
				}
			}

			Collections.shuffle(BIRTHS_SAMPLES);
		} finally {
			is.close();
			br.close();
		}
	}

	protected static void loadReutersData() throws IOException {
		Map<String, Integer> topics = new HashMap<String, Integer>();

		Random random = new Random();

		FileInputStream is = new FileInputStream(REUTEURS_FILE);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				try {
					// Get class index
					String topic = line.split(",")[0];
					if (!topics.containsKey(topic)) {
						topics.put(topic, topics.size());
					}
					Integer classIndex = topics.get(topic);

					// Get text
					int startIndex = line.indexOf(" - ");
					String text = line.substring(startIndex, line.length() - 1);

					// Add to train or eval
					Map<Integer, List<String>> data = random.nextDouble() < 0.80 ? REUTERS_TRAIN_DATA : REUTERS_EVAL_DATA;
					if (!data.containsKey(classIndex)) {
						data.put(classIndex, new ArrayList<String>());
					}
					data.get(classIndex).add(text);
				} catch (Exception ex) {
					System.out.println("Skipped Reuters sample : " + line);
				}
			}

		} finally {
			is.close();
			br.close();
		}
	}

	public static List<Instance<Boolean>> generatedNandInstances(int nb) {
		Random random = new Random();

		List<Instance<Boolean>> samples = new ArrayList<Instance<Boolean>>();
		for (int i = 0; i < nb; i++) {
			List<Boolean> nandInputs = Arrays.asList(random.nextBoolean(), random.nextBoolean());
			Boolean label = !(nandInputs.get(0) && nandInputs.get(1));
			double[] features = new double[] { 1.0, nandInputs.get(0) ? 1.0 : -1.0, nandInputs.get(1) ? 1.0 : -1.0 };
			samples.add(new Instance<Boolean>(label, features));
		}

		return samples;
	}

	public static List<Instance<Boolean>> generateDataForClassification(int size, int featureSize) {
		Random random = new Random();
		List<Instance<Boolean>> samples = new ArrayList<Instance<Boolean>>();

		for (int i = 0; i < size; i++) {
			Double label = random.nextDouble() > 0.5 ? 1.0 : -1.0;
			double[] features = new double[featureSize + 1];
			for (int j = 0; j < featureSize; j++) {
				features[j] = (j % 2 == 0 ? 1.0 : -1.0) * label + random.nextDouble() - 0.5;
			}
			features[featureSize] = 1.0;
			samples.add(new Instance<Boolean>(label > 0, features));
		}

		return samples;
	}

	public static List<Instance<Integer>> generateDataForMultiLabelClassification(int size, int featureSize, int nbClasses) {
		Random random = new Random();
		List<Instance<Integer>> samples = new ArrayList<Instance<Integer>>();

		for (int i = 0; i < size; i++) {
			Integer label = random.nextInt(nbClasses);
			double[] features = new double[featureSize];
			for (int j = 0; j < featureSize; j++) {
				features[j] = (j % (label + 1) == 0 ? 1.0 : -1.0) + random.nextDouble() - 0.5;
			}
			samples.add(new Instance<Integer>(label, features));
		}

		return samples;
	}

	public static List<Instance<Double>> generateDataForRegression(int size, int featureSize) {
		List<Instance<Double>> samples = new ArrayList<Instance<Double>>();

		Random random = new Random();
		List<Double> factors = new ArrayList<Double>(featureSize);
		for (int i = 0; i < featureSize; i++) {
			factors.add(random.nextDouble() * (1 + random.nextInt(2)));
		}

		for (int i = 0; i < size; i++) {
			double label = 0.0;

			double[] features = new double[featureSize];
			for (int j = 0; j < featureSize; j++) {
				double feature = (j % 2 == 0 ? 1.0 : -1.0) * random.nextDouble();
				features[j] = feature;
				label += factors.get(j) * feature;
			}

			samples.add(new Instance<Double>(label, features));
		}

		return samples;
	}
}
