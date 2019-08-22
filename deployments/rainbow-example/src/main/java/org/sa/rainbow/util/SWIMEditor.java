package org.sa.rainbow.util;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sa.rainbow.util.SWIMDriver.Closure;

public class SWIMEditor {
	
	class Closure {
		Integer currentSecond = 1;
		Double currentSum = 0.0;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println(usage());
			System.exit(1);
		}
		String filename = args[0];
		if (!new File(filename).exists()) {
			System.err.println(usage());
			System.exit(1);
		}
		new SWIMEditor().go(filename);
	}

	private static String usage() {
		return "Usage: prog filename";
	}

	private void go(String filename) {
		Map<Integer, List<Double>> arrivalRateMap = getArrivalRates(filename);
		
		List<Integer> arrivalRate = arrivalRateMap.values().stream().map(l -> l.size()).collect(Collectors.toList());
		SimulationEditorWindow window = new SimulationEditorWindow();
		window.setData(arrivalRate, arrivalRateMap);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window.m_frame.setSize(520, 300);
					window.m_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	protected Map<Integer, List<Double>> getArrivalRates(String filename) {
		Map<Integer, List<Double>> arrivalRateMap = new LinkedHashMap<>();

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
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return arrivalRateMap;
	}

	private Map<Integer, List<Double>> getArrivalRate(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

}
