package org.sa.rainbow.stitch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.sa.rainbow.stitch.model.ModelRepository;

public class CommandLineModelRepository implements ModelRepository {

	private HashMap<String, IAcmeResource> m_path2Resource;

	public CommandLineModelRepository () {
		m_path2Resource = new HashMap<String, IAcmeResource> ();
	}
	
	@Override
	public Object getModelForResource(String resName) throws IOException {
		IAcmeResource ar = m_path2Resource.get (resName);
		if (ar == null) {
			try {
				ar = StandaloneResourceProvider.instance ().acmeResourceForString (resName);
				m_path2Resource.put (resName, ar);
			}
			catch (ParsingFailureException e) {
				e.printStackTrace ();
				return null;
			}
		}
		return ar.getModel ();
	}

	@Override
	public Object getSnapshotModel(Object model) {
		return model;
	}

	@Override
	public void markDisruption(double level) {
		// TODO Auto-generated method stub

	}

	@Override
	public File tacticExecutionHistoryFile() {
		// TODO Auto-generated method stub
		return null;
	}

}
