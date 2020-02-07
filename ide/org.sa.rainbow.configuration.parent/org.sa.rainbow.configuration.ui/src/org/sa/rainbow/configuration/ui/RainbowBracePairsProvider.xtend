package org.sa.rainbow.configuration.ui

import com.google.inject.Singleton
import java.util.Set
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ide.editor.bracketmatching.BracePair
import org.eclipse.xtext.ide.editor.bracketmatching.IBracePairProvider

@Singleton
@Accessors
@FinalFieldsConstructor
class RainbowBracePairsProvider  implements IBracePairProvider {

	val Set<BracePair> pairs


	new() {
		this(#{
			new BracePair("(", ")", true),
			new BracePair("{", "}", true),
			new BracePair("[", "]", true),
			new BracePair("\u00AB", "\u00BB", true)
		})
	}
	
}