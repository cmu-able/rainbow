package org.sa.rainbow.gui.visitor;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.sa.rainbow.gui.arch.model.RainbowArchAdapationManagerModel;
import org.sa.rainbow.gui.arch.model.RainbowArchAnalysisModel;
import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchGaugeModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;
import org.sa.rainbow.gui.arch.model.RainbowArchProbeModel;
import org.sa.rainbow.gui.arch.model.RainbowSystemModel;
import org.sa.rainbow.gui.arch.model.RainbowSystemModel.IRainbowModelVisitor;

public class DOTStringGraphConstructor implements IRainbowModelVisitor {
	
	private Map<String,StringBuilder> processedIds;
	public int res = 80;
	private RainbowSystemModel m_system;
	private Map<String,String> model2am;
	private HashSet<String> executorNodes;
	public StringBuilder m_graph;
	
	protected float toInches(int unit, float res) {
		return unit / res;
	}
	
	protected void setNodeAttributes(RainbowArchModelElement model, StringBuilder node) {
		Dimension size = model.getController().getView().getSize();
		node.append("width=\""); node.append(toInches(size.width, res)); node.append("\";");
		node.append("height=\""); node.append(toInches(size.height, res)); node.append("\";");
		node.append("fixedsize=true;");
		node.append("shape=box;");
		Point loc;
		if ((loc = model.getLocation()) != null) {
			node.append("pos=\""); 
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("");
			stringBuilder.append(toInches(loc.x + size.width/2, res));
			stringBuilder.append(",");
			stringBuilder.append(toInches(loc.y + size.height/2,res));
			node.append(stringBuilder.toString());
			node.append("\";");
		}
	}

	@Override
	public void visitSystem(RainbowSystemModel model) {
		m_graph = new StringBuilder();
		m_graph.append("graph rainbow {\n");
		m_graph.append("splines=\"compound\"; ");
		m_graph.append("rankdir=\"BT\";\n");
		processedIds = new HashMap();
		model2am = new HashMap<>();
		executorNodes = new HashSet<>();
		m_system = model;
	}
	
	public void postVisitSystem(RainbowSystemModel model) {
		StringBuilder same = new StringBuilder("same; ");
		for (RainbowArchProbeModel probe : model.getProbes()) {
			same.append("\"").append(probe.getId()).append("\";");
		}
		
		for (RainbowArchEffectorModel eff : model.getEffectors()) {
			same.append("\"").append(eff.getId()).append("\";");
		}
		m_graph.append("{rank=").append(same.toString()).append("};\n").append("}\n");
	}

	@Override
	public void visitProbe(RainbowArchProbeModel probeInfo) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(probeInfo.getId()).append("\" [");
		setNodeAttributes(probeInfo, node);
		node.append("];\n");
		processedIds.put(probeInfo.getId(), node);
		m_graph.append(node.toString());
	}

	

	@Override
	public void visitGauge(RainbowArchGaugeModel gauge) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(gauge.getId()).append("\" [");
		setNodeAttributes(gauge, node);
		node.append("];\n");
		m_graph.append(node.toString());
		processedIds.put(gauge.getId(), node);
		for (String probe : gauge.getProbes()) {
			m_graph.append("\"").append(gauge.getId()).append("\" -- \"").append(probe).append("\";\n");
		}
	}

	@Override
	public void visitModel(RainbowArchModelModel model) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(model.getId()).append("\" [");
		setNodeAttributes(model, node);
		node.append("];\n");
		m_graph.append(node.toString());
		processedIds.put(model.getId(), node);
		for (String ga : model.getGaugeReferences()) {
			if (processedIds.containsKey(ga)) {
				m_graph.append("\"").append(model.getId()).append("\" -- \"").append(ga).append("\";\n");
			}
		}
	}

	@Override
	public void visitAnalysis(RainbowArchAnalysisModel analysis) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(analysis.getId()).append("\" [");
		setNodeAttributes(analysis, node);
		node.append("];\n");
		processedIds.put(analysis.getId(), node);
		m_graph.append(node.toString());
		for (RainbowArchModelModel m : m_system.getModels()) {
			if (processedIds.containsKey(m.getId()))
				m_graph.append("\"").append(analysis.getId()).append("\" -- \"").append(m.getId()).append("\";\n");
		}
	}

	@Override
	public void visitAdaptationManager(RainbowArchAdapationManagerModel adaptationManager) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(adaptationManager.getId()).append("\" [");
		setNodeAttributes(adaptationManager, node);
		node.append("];\n");
		m_graph.append(node.toString());
		String model = adaptationManager.getAdaptationManager().getManagedModel().toString();
		model2am.put(model,  adaptationManager.getId());
		processedIds.put(adaptationManager.getId(), node);
		StringBuilder n = processedIds.get(model);
		if (n != null) {
			m_graph.append("\"").append(adaptationManager.getId()).append("\" -- \"").append(model).append("\";\n");
		}
	}

	@Override
	public void visitExecutor(RainbowArchExecutorModel executor) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(executor.getId()).append("\" [");
		setNodeAttributes(executor, node);
		node.append("];\n");
		m_graph.append(node.toString());
		processedIds.put(executor.getId(), node);
		String model = executor.getExecutor().getManagedModel().toString();
		StringBuilder n = processedIds.get(model);
		if (n != null) {
			m_graph.append("\"").append(executor.getId()).append("\" -- \"").append(model).append("\";\n");
		}
		String aM = model2am.get(model);
		if (aM != null) {
			m_graph.append("\"").append(executor.getId()).append("\" -- \"").append(aM).append("\";\n");

		}
		executorNodes.add(executor.getId());

	}

	@Override
	public void visitEffector(RainbowArchEffectorModel effector) {
		StringBuilder node = new StringBuilder();
		node.append("\"").append(effector.getId()).append("\" [");
		setNodeAttributes(effector, node);
		for (String ex : executorNodes) {
			m_graph.append("\"").append(ex).append("\" -- \"").append(effector.getId()).append("\";\n");
		}
	}

}
