package org.sa.rainbow.configuration.ui.contentassist

import com.google.inject.Inject
import java.util.List
import org.eclipse.jface.text.templates.ContextTypeRegistry
import org.eclipse.jface.text.templates.Template
import org.eclipse.jface.text.templates.TemplateContext
import org.eclipse.jface.text.templates.persistence.TemplateStore
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ITemplateAcceptor
import org.eclipse.xtext.ui.editor.templates.ContextTypeIdHelper
import org.eclipse.xtext.ui.editor.templates.DefaultTemplateProposalProvider
import org.sa.rainbow.configuration.ModelUtil
import org.sa.rainbow.configuration.ModelUtil.CommandRep
import org.sa.rainbow.configuration.rcl.EffectorBody
import org.sa.rainbow.configuration.rcl.Gauge
import org.sa.rainbow.configuration.rcl.GaugeTypeBody
import org.sa.rainbow.configuration.services.RclGrammarAccess

class RainbowTemplateProposalProvider extends DefaultTemplateProposalProvider {

	val ContextTypeIdHelper helper;

	@Inject
	new(TemplateStore templateStore, ContextTypeRegistry registry, ContextTypeIdHelper helper) {
		super(templateStore, registry, helper)
		this.helper = helper
	}

	@Inject
	RclGrammarAccess ga;

	override protected createTemplates(TemplateContext templateContext, ContentAssistContext context,
		ITemplateAcceptor acceptor) {
		super.createTemplates(templateContext, context, acceptor)

		val prID = helper.getId(ga.propertyReferenceRule)
		if (prID == templateContext.contextType.id) {
			val template = new Template("name::type", "Old syle model reference", "modelRefNameAndType",
				"${modelName}::${modelType}", false)
			val tp = createProposal(template, templateContext, context, null, getRelevance(template))
			acceptor.accept(tp)
			return
		}

		val model = context.currentModel
		val gauge = EcoreUtil2.getContainerOfType(model, Gauge)
		val gaugeType = EcoreUtil2.getContainerOfType(model, GaugeTypeBody)
		val effector = EcoreUtil2.getContainerOfType(model, EffectorBody)
		val container = gauge != null ? gauge : gaugeType != null ? gaugeType : effector != null ? effector : null

		if (helper.getId(ga.commandCallRule) == templateContext.contextType.id ||
			helper.getId(ga.commandReferenceRule) == templateContext.contextType.id) {
			switch container {
				EffectorBody:
					templatesForFactory(ModelUtil.getCommandsFromReference(container.ref, null, null, false),
						templateContext, context, acceptor, false, false)
				GaugeTypeBody:
					templatesForFactory(ModelUtil.getCommandsFromReference(container.mcf, null, container.commands.map [
						it.command
					].toList, true), templateContext, context, acceptor, true, true)
				Gauge:
					templatesForFactory(
						ModelUtil.getCommandsFromReference(container?.superType?.body?.mcf,
							container?.superType?.body?.commands?.map[it.command]?.toList(), container.body.commands.map [
								it.command
							].toList, true), templateContext, context, acceptor, true, false)
			}
			return
		}

	}

	def templatesForFactory(List<CommandRep> reps, TemplateContext templateContext, ContentAssistContext context,
		ITemplateAcceptor acceptor, boolean isGauge, boolean isGaugeType) {
		reps.forEach [
			var template = new Template('''command «it.name»''', '''Call for command «it.name»''',
				templateContext.contextType.
					id, '''command «IF isGauge»${name} = «ENDIF»«IF it.hasTarget !== null»${«it.hasTarget»}.«ENDIF»«it.name»(«FOR f : it.formalNames SEPARATOR ', '»${«f»}«ENDFOR»)''',
				true)
			var tp = createProposal(template, templateContext, context, null, getRelevance(template))
			acceptor.accept(tp)
			if (isGaugeType) {
				template = new Template('''command «it.name» with regexp''', '''Command «it.name» with regexp''',
					templateContext.contextType.
						id, '''command ${name} = ${regexmp} -> «IF it.hasTarget !== null»${«it.hasTarget»}.«ENDIF»«it.name»(«FOR f : it.formalNames SEPARATOR ', '»${«f»}«ENDFOR»)''',
					true)
				tp = createProposal(template, templateContext, context, null, getRelevance(template))
				acceptor.accept(tp)
			}
		]
	}

}
