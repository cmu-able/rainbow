package org.sa.rainbow.util;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SWIMDriver {

	class Closure {
		Integer currentSecond = 1;
		Double currentSum = 0.0;
	}

	protected List<Integer> getArrivalRate(String filename) {
		Map<Integer, List<Double>> arrivalRateMap = new LinkedHashMap<>();
		List<Integer> arrivalRate = Collections.<Integer>emptyList();

		try {
			Closure closure = new Closure();

			Files.lines(new File(filename).toPath()).map(s -> Double.parseDouble(s.trim())).forEach(d -> {
				closure.currentSum += d;
				if (closure.currentSum > 1) {
					closure.currentSecond++;
					closure.currentSum = d;
				}
				if (!arrivalRateMap.containsKey(closure.currentSecond))
					arrivalRateMap.put(closure.currentSecond, new LinkedList<Double>());
				arrivalRateMap.get(closure.currentSecond).add(d);
			});

			arrivalRate = arrivalRateMap.values().stream().map(l -> l.size()).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return arrivalRate;
	}

	public static void main(String[] args) throws IOException {
		new SWIMDriver().go();
	}

	private void go() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		String wd = "/headless/seams-swim/swim/simulations/swim/";
		props.load(new FileInputStream(wd + "swim.ini"));
		
		String property = props.getProperty("*.source.interArrivalsFile");
		String[] files = property.split("=")[1].trim().split(",");
		
		SimulationSelectionWindow window = new SimulationSelectionWindow();
		for (String f : files) {
			f = f.replaceAll("\\\"", "").trim();
			List<Integer> ar = getArrivalRate(wd + f);
			window.addSimulationSeries(ar, f);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window.m_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
