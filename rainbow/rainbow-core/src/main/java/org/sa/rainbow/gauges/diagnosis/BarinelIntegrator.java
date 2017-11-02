package org.sa.rainbow.gauges.diagnosis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

/**
 * Class that performs integration with barinel obtaining a diagnosis from
 * a spectra snapshot.
 */
public class BarinelIntegrator {
	/**
	 * Where the staccato executable lies.
	 */
	private File m_staccato;
	
	/**
	 * The barinel executable.
	 */
	private File m_barinel;
	
	/**
	 * Listeners of the barinel integrator.
	 */
	private List<BarinelIntegratorListener> m_listeners;
	
	/**
	 * Next run ID.
	 */
	private int m_next_id;
	
	/**
	 * Creates a new integrator using the default implementation of barinel.
	 * @throws IOException failed to initialize the integrator
	 */
	public BarinelIntegrator() throws IOException {
		String staccato_rsrc_name = "staccato-" + SystemUtils.OS_NAME + "-"
				+ SystemUtils.OS_ARCH;
		String barinel_rsrc_name = "barinel-" + SystemUtils.OS_NAME + "-"
				+ SystemUtils.OS_ARCH;
		
		File staccato_f = File.createTempFile("staccato", ".bin");
		staccato_f.setExecutable(true);
		staccato_f.deleteOnExit();
		
		try (FileOutputStream staccato_out = new FileOutputStream(staccato_f);
				InputStream staccato_rsrc = getClass().getResourceAsStream(
						staccato_rsrc_name)) {
			if (staccato_rsrc == null) {
				throw new IOException("Failed to find staccato resource '"
						+ staccato_rsrc_name + "'.");
			}
			
			int ch;
			while ((ch = staccato_rsrc.read()) != -1) {
				staccato_out.write(ch);
			}
		}
		
		File barinel_f = File.createTempFile("barinel", ".bin");
		barinel_f.setExecutable(true);
		barinel_f.deleteOnExit();
		
		try (FileOutputStream barinel_out = new FileOutputStream(barinel_f);
				InputStream barinel_rsrc = getClass().getResourceAsStream(
						barinel_rsrc_name)) {
			if (barinel_rsrc == null) {
				throw new IOException("Failed to find barinel resource '"
						+ barinel_rsrc_name + "'.");
			}
			
			int ch;
			while ((ch = barinel_rsrc.read()) != -1) {
				barinel_out.write(ch);
			}
		}
		
		init(staccato_f, barinel_f);
	}
	
	/**
	 * Creates a new integrator.
	 * @param staccato the staccato executable
	 * @param barinel the barinel executable
	 * @throws IOException failed to initialize the integrator
	 */
	public BarinelIntegrator(File staccato, File barinel) throws IOException {
		if (staccato == null) {
			throw new IllegalArgumentException("staccato == null");
		}
		
		if (barinel == null) {
			throw new IllegalArgumentException("barinel == null");
		}
		
		init(staccato, barinel);
	}
	
	/**
	 * Initializes the integrator.
	 * @param staccato the staccato executable
	 * @param barinel the barinel executable
	 * @throws IOException failed to initialize the integrator
	 */
	private void init(File staccato, File barinel) throws IOException {
		if (!staccato.canExecute()) {
			throw new IOException("staccato executable '"
					+ staccato.getAbsolutePath() + "' not found or not "
					+ "executable.");
		}
		
		if (!barinel.canExecute()) {
			throw new IOException("barinel executable '"
					+ barinel.getAbsolutePath() + "' not found or not "
					+ "executable.");
		}
		
		m_staccato = staccato;
		m_barinel = barinel;
		m_listeners = new ArrayList<>();
		m_next_id = 1;
	}
	
	/**
	 * Adds a new barinel integration listener.
	 * @param l the listener
	 */
	public synchronized void addBarinelIntegratorListener(
			BarinelIntegratorListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}
		
		m_listeners.add(l);
	}
	
	/**
	 * Removes a previously added barinel integration listener.
	 * @param l the listener
	 */
	public synchronized void removeBarinelIntegratorListener(
			BarinelIntegratorListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}
		
		if (!m_listeners.remove(l)) {
			throw new IllegalStateException("Listener not registered.");
		}
	}

	/**
	 * Obtains the set of all architectural elements that are in a snapshot.
	 * @param snapshot the snapshot
	 * @return the set of architectural elements
	 */
	private List<String> identify_arch_elements(List<Spectrum> snapshot) {
		assert snapshot != null;
		
		Set<String> arch_elements = new HashSet<String>();

		for (Spectrum s : snapshot) {
			arch_elements.addAll(s.getElements());
		}

		return new ArrayList<>(new TreeSet<>(arch_elements));
	}
	
	/**
	 * Generates an array of booleans corresponding to each element in the
	 * list of architectural elements depending on whether each element is used
	 * or not in the spectrum.
	 * @param s the spectrum
	 * @param arch_elements the list of elements
	 * @return the boolean array
	 */
	private boolean[] used(Spectrum s, List<String> arch_elements) {
		assert s != null;
		assert arch_elements != null;
		assert arch_elements.size() > 0;
		
		boolean[] u = new boolean[arch_elements.size()];
		for (String e : s.getElements()) {
			int idx = arch_elements.indexOf(e);
			assert idx >= 0;
			assert u[idx] == false;
			u[idx] = true;
		}
		
		return u;
	}
	
	/**
	 * Generates the input file for staccato.
	 * @param snapshot the snapshot
	 * @param arch_elements the list of elements
	 * @return the file
	 * @throws IOException I/O failed
	 */
	private File generate_staccato_file(List<Spectrum> snapshot,
			List<String> arch_elements) throws IOException {
		assert snapshot != null;
		assert snapshot.size() > 0;
		assert arch_elements != null;
		assert arch_elements.size() > 0;
		
		File s_in = File.createTempFile("staccato_in", ".data");
		FileWriter fw_in = new FileWriter(s_in);
		
		for (Spectrum s : snapshot) {
			boolean[] u = used(s, arch_elements);
			for (boolean ui : u) {
				if (ui) {
					fw_in.write("1 ");
				} else {
					fw_in.write("0 ");
				}
			}
			
			if (s.isCorrect()) {
				fw_in.write("+");
			} else {
				fw_in.write("-");
			}
			
			fw_in.write(SystemUtils.LINE_SEPARATOR);
		}
		
		fw_in.close();
		return s_in;
	}
	
	/**
	 * Runs staccato in an input file.
	 * @param input the input file
	 * @param arch_elements the architectural elements
	 * @return the output file
	 * @throws IOException failed to run staccato
	 * @throws InterruptedException failed to wait for staccato
	 */
	private File run_staccato(File input, List<String> arch_elements)
			throws IOException, InterruptedException {
		assert input != null;
		assert input.isFile();
		assert arch_elements != null;
		assert arch_elements.size() > 0;
		
		ProcessBuilder pb = new ProcessBuilder(m_staccato.getAbsolutePath(),
				"-o", "" + arch_elements.size(), input.getAbsolutePath());
		Process staccato = pb.start();
		InputStreamReader stin = new InputStreamReader(
				staccato.getInputStream());
		int result = staccato.waitFor();
		if (result != 0) {
			throw new IOException("staccato ended with result code " + result);
		}

		File staccato_out = File.createTempFile("staccato_out", ".data");
		FileWriter fw_out = new FileWriter(staccato_out);
		
		int ch;
		while ((ch = stin.read()) != -1) {
			fw_out.write(ch);
		}

		fw_out.close();
		stin.close();
		
		return staccato_out;
	}
	
	/**
	 * Runs the barinel executable.
	 * @param staccato_input the staccato input file
	 * @param staccato_output the staccato output file
	 * @param arch_elements the architectural elements
	 * @return the program output
	 * @throws IOException failed to run the program
	 * @throws InterruptedException failed to wait for barinel
	 */
	private String run_barinel(File staccato_input, File staccato_output,
			List<String> arch_elements) throws IOException,
			InterruptedException {
		assert staccato_input != null;
		assert staccato_input.canRead();
		assert staccato_output != null;
		assert staccato_output.canRead();
		
		ProcessBuilder pb = new ProcessBuilder(m_barinel.getAbsolutePath(),
				"-f", staccato_output.getAbsolutePath(), "-o",
				"" + arch_elements.size(), staccato_input.getAbsolutePath());
		Process mbr = pb.start();
		InputStreamReader mbin = new InputStreamReader(mbr.getInputStream());
		int result = mbr.waitFor();
		if (result != 0) {
			throw new IOException("barinel ended with result code " + result);
		}

		int ch;
		StringWriter sw = new StringWriter();
		while ((ch = mbin.read()) != -1) {
			sw.write(ch);
		}

		mbin.close();
		sw.close();
		
		return sw.toString();
	}
	
	/**
	 * Produces a report for a snapshot of spectra.
	 * @param snapshot the spectra
	 * @return the report
	 * @throws IOException failed to evaluate
	 * @throws InterruptedException failed to wait for either staccato or
	 * barinel
	 */
	public DiagnosisReport evaluate(List<Spectrum> snapshot)
			throws IOException, InterruptedException {
		if (snapshot == null) {
			throw new IllegalArgumentException("snapshot == null");
		}
		
		List<BarinelIntegratorListener> lcp;
		int id;
		
		synchronized(this) {
			lcp = new ArrayList<>(m_listeners);
			id = m_next_id;
			m_next_id++;
		}
		
		int suc = 0;
		int fail = 0;
		
		for (Spectrum s : snapshot) {
			if (s.isCorrect()) {
				suc++;
			} else {
				fail++;
			}
		}
		
		for (BarinelIntegratorListener l : lcp) {
			l.staccato_started(id, suc, fail);
		}
		
		/*
		 * No need to run anything if there are no failures.
		 */
		if (fail == 0) {
			/*
			 * Gotta keep listeners thinking we did everything :)
			 */
			for (BarinelIntegratorListener l : lcp) {
				l.barinel_started(id);
				l.diagnosis_completed(id, 0);
			}
			
			return new DiagnosisReport(new HashSet<FaultCandidate>());
		}
		
		List<String> arch_elements = identify_arch_elements(snapshot);
		File staccato_in = generate_staccato_file(snapshot, arch_elements);
		File staccato_out = run_staccato(staccato_in, arch_elements);
		
		for (BarinelIntegratorListener l : lcp) {
			l.barinel_started(id);
		}
		
		String barinel_out = run_barinel(staccato_in, staccato_out,
				arch_elements);
		
		Set<FaultCandidate> candidates = new HashSet<>();
		
		/*
		 * Now parse barinel's output.
		 */
		String[] barinel_out_lines = barinel_out.split("\\n");
		for (int i = 0; i < barinel_out_lines.length; i++) {
			Pattern p = Pattern.compile("\\{?([^}]*)\\}?\\s+(\\S+)");
			Matcher m = p.matcher(barinel_out_lines[i]);
			if (!m.find()) {
				throw new IOException("Unknown barinel output line: "
						+ barinel_out_lines[i]);
			}
			
			if (m.group(1).trim().length() > 0) {
				String[] parts = m.group(1).split(",");
				Set<String> candidate_elements = new HashSet<>();
				for (String ptxt : parts) {
					int pnumber = Integer.parseInt(ptxt.trim()) - 1;
					if (pnumber < 0 || pnumber >= arch_elements.size()) {
						throw new IOException("Line '" + barinel_out_lines[i]
								+ "' referes to element " + pnumber + " which "
								+ "does not exist.");
					}
					
					candidate_elements.add(arch_elements.get(pnumber));
				}

				float prob = Float.parseFloat(m.group(2));
				candidates.add(new FaultCandidate(candidate_elements, prob));
			}
		}
		
		/*
		 * Generate the report and return.
		 */
		DiagnosisReport dr = new DiagnosisReport(candidates);
		
		staccato_in.delete();
		staccato_out.delete();
		
		for (BarinelIntegratorListener l : lcp) {
			l.diagnosis_completed(id, candidates.size());
		}
		
		return dr;
	}
}
