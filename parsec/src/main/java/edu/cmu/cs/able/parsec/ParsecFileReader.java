package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class providing support to read a file and generate a {@link TextContainer}.
 */
public class ParsecFileReader {
	/**
	 * Creates a new reader.
	 */
	public ParsecFileReader() {
	}
	
	/**
	 * Creates a text container from a file.
	 * @param file the file
	 * @return the container
	 * @throws IOException failed to read the file
	 */
	public TextContainer read(File file) throws IOException {
		Ensure.not_null(file);
		
		TextFile text_file = new TextFile(file);
		TextRegion region = new TextRegion(text_file);
		List<TextRegion> regions = new ArrayList<>();
		regions.add(region);
		
		return new TextContainer(regions);
	}
	
	/**
	 * Creates a text container from an in-memory string.
	 * @param text the text
	 * @return the container
	 */
	public TextContainer read_memory(String text) {
		Ensure.not_null(text);
		
		TextRegion region = new TextRegion(text);
		List<TextRegion> regions = new ArrayList<>();
		regions.add(region);
		
		return new TextContainer(regions);
	}
}
