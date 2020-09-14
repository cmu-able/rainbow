package incubator.scb.sdl;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm that runs several generators.
 */
public class GenerationAlgorithm {
	/**
	 * Utility class: no constructor.
	 */
	private GenerationAlgorithm() {
		/*
		 * Nothing to do.
		 */
	}
	
	/**
	 * Generates data for a set of given generators. It keeps generating
	 * iteratively while it manages to generate code.
	 * @param generators the list of generators, which may be empty
	 * @return the result of generation
	 * @throws SdlGenerationException failed to generate
	 */
	public static GenerationInfo generate(List<Generator> generators)
			throws SdlGenerationException {
		Ensure.not_null(generators, "generators == null");
		
		boolean generated_any = false;
		boolean can_rerun;
		GenerationInfo r;
		do {
			can_rerun = false;
			
			List<GenerationInfo> gis = new ArrayList<>();
			for (Generator g : generators) {
				GenerationInfo gi = g.generate();
				Ensure.not_null(gi, "gi == null");
				if (gi.result() == GenerationResult.GENERATED_CODE) {
					can_rerun = true;
					generated_any = true;
				}
				
				gis.add(gi);
			}
			
			r = new GenerationInfo(gis);
		} while (can_rerun);
		
		if (r.result() == GenerationResult.NOTHING_TO_DO && generated_any) {
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return r;
		}
	}
}
