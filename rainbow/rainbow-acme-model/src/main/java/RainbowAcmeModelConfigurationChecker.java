import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;

import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.util.IRainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;
import org.sa.rainbow.util.Util;

public class RainbowAcmeModelConfigurationChecker implements IRainbowConfigurationChecker {

	private LinkedList<Problem> m_problems = new LinkedList<>();
	private IRainbowMaster m_master;

	public RainbowAcmeModelConfigurationChecker() {
		m_problems = new LinkedList<Problem>();
	}

	@Override
	public void checkRainbowConfiguration() {
		String message = "Checking that Acme files parse...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();

		String numberOfModelsStr = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_NUMBER, "0");
		int numberOfModels = Integer.parseInt(numberOfModelsStr);
		for (int modelNum = 0; modelNum < numberOfModels; modelNum++) {
			String factoryClassName = Rainbow.instance()
					.getProperty(RainbowConstants.PROPKEY_MODEL_LOAD_CLASS_PREFIX + modelNum);
			if (factoryClassName == null || "".equals(factoryClassName)) {
				continue;
			}
			String modelName = "";
			File modelPath = new File("");
			try {
				Class loadClass = Class.forName(factoryClassName);
				if (AcmeModelCommandFactory.class.isAssignableFrom(loadClass)) {
					modelName = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_NAME_PREFIX + modelNum);
					String path = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_PATH_PREFIX + modelNum);
					modelPath = null;
					if (path != null) {
						modelPath = new File(path);
						if (!modelPath.isAbsolute()) {
							modelPath = Util.getRelativeToPath(Rainbow.instance().getTargetPath(), path);
						}

					}
					IAcmeResource resource = StandaloneResourceProvider.instance().acmeResourceForObject(modelPath);
				}
			} catch (ClassNotFoundException e) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("Could not create the class {0} to load a model", factoryClassName)));
			} catch (ParsingFailureException e) {
				for (AcmeError err : e.getErrors()) {
					m_problems.add(new Problem(ProblemT.ERROR,
							MessageFormat.format("{0}: Error in Acme: {1}", modelName, e.getMessage())));
				}
			} catch (IOException e) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("Could not load the file {0}", modelPath.getAbsolutePath())));
			}

		}
		if (num == m_problems.size()) p.setMessage(message + "ok");
	}

	@Override
	public void setRainbowMaster(IRainbowMaster master) {
		m_master = master;
	}

	@Override
	public Collection<Problem> getProblems() {
		return m_problems;
	}

}
