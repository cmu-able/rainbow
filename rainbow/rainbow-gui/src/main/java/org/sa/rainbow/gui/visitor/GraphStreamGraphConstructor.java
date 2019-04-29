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

public class GraphStreamGraphConstructor implements IRainbowModelVisitor {
	
	public Graph m_graph;
	private Map<String,Node> processedIds;
	public int res = 80;
	private RainbowSystemModel m_system;
	private Map<String,Node> model2am;
	private HashSet<Node> executorNodes;
	
	protected float toInches(int unit, float res) {
		return unit / res;
	}
	
	protected void setNodeAttributes(RainbowArchModelElement model, Node node) {
		Dimension size = model.getController().getView().getSize();
		node.addAttribute("width", toInches(size.width, res));
		node.addAttribute("height", toInches(size.height, res));
		node.addAttribute("fixedsize", true);
		node.addAttribute("shape", "box");
		Point loc;
		if ((loc = model.getLocation()) != null) {
			node.addAttribute("pos", "" + toInches(loc.x + size.width/2, res) + "," + toInches(loc.y + size.height/2,res));
		}
	}

	@Override
	public void visitSystem(RainbowSystemModel model) {
		m_graph = new SingleGraph("rainbow-system");
		processedIds = new HashMap();
		model2am = new HashMap<>();
		executorNodes = new HashSet<Node>();
		
		m_graph.setAttribute("splines", "compound");
		m_graph.setAttribute("rankdir", "BT");
		m_system = model;
	}
	
	public void postVisitSystem(RainbowSystemModel model) {
		StringBuffer same = new StringBuffer("same; ");
		for (RainbowArchProbeModel probe : model.getProbes()) {
			same.append(probe.getId());
			same.append(";");
		}
		
		for (RainbowArchEffectorModel eff : model.getEffectors()) {
			same.append(eff.getId());
			same.append(";");
		}
		m_graph.setAttribute("rank", same.toString());
	}

	@Override
	public void visitProbe(RainbowArchProbeModel probeInfo) {
		Node pN = m_graph.addNode(probeInfo.getId());
		setNodeAttributes(probeInfo, pN);
		processedIds.put(probeInfo.getId(), pN);
	}

	

	@Override
	public void visitGauge(RainbowArchGaugeModel gauge) {
		Node gN = m_graph.addNode(gauge.getId());
		setNodeAttributes(gauge, gN);
		processedIds.put(gauge.getId(), gN);
		for (String probe : gauge.getProbes()) {
			Node pN = processedIds.get(probe);
			m_graph.addEdge(pN.getId() + "-" + gN.getId(), gN, pN);
		}
	}

	@Override
	public void visitModel(RainbowArchModelModel model) {
		Node mN = m_graph.addNode(model.getId());
		setNodeAttributes(model, mN);
		processedIds.put(model.getId(), mN);
		for (String ga : model.getGaugeReferences()) {
			if (processedIds.containsKey(ga))
				m_graph.addEdge(ga + "-" + mN.getId(), mN, processedIds.get(ga));
		}
	}

	@Override
	public void visitAnalysis(RainbowArchAnalysisModel analysis) {
		Node aN = m_graph.addNode(analysis.getId());
		setNodeAttributes(analysis, aN);
		processedIds.put(analysis.getId(), aN);
		for (RainbowArchModelModel m : m_system.getModels()) {
			if (processedIds.containsKey(m.getId()))
				m_graph.addEdge(aN.getId() + "-" + m, aN, processedIds.get(m.getId()));
		}
	}

	@Override
	public void visitAdaptationManager(RainbowArchAdapationManagerModel adaptationManager) {
		Node aN = m_graph.addNode(adaptationManager.getId());
		setNodeAttributes(adaptationManager, aN);
		String model = adaptationManager.getAdaptationManager().getManagedModel().toString();
		model2am.put(model,  aN);
		processedIds.put(adaptationManager.getId(), aN);
		Node n = processedIds.get(model);
		if (n != null) {
			m_graph.addEdge(aN.getId() + "-" + n.getId(), aN, n);
		}
	}

	@Override
	public void visitExecutor(RainbowArchExecutorModel executor) {
		Node aN = m_graph.addNode(executor.getId());
		setNodeAttributes(executor, aN);
		processedIds.put(executor.getId(), aN);
		String model = executor.getExecutor().getManagedModel().toString();
		Node n = processedIds.get(model);
		if (n != null) {
		m_graph.addEdge(aN.getId() + "-" + n.getId(), aN, n);

		}
		Node aM = model2am.get(model);
		if (aM != null) {
			m_graph.addEdge(aM.getId() + "-" + aN.getId(), aM, aN);
		}
		executorNodes.add(aN);

	}

	@Override
	public void visitEffector(RainbowArchEffectorModel effector) {
		Node eN = m_graph.addNode(effector.getId());
		setNodeAttributes(effector, eN);
		for (Node ex : executorNodes) {
			m_graph.addEdge(ex.getId() + "-" + eN.getId(), ex, eN);
		}
	}

}
