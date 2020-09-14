package org.sa.rainbow.stitch;
/*
Copyright 2020 Carnegie Mellon University

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software 
without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.
 */
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;

public class RainbowOutputConfigurationProvider implements IOutputConfigurationProvider {

	public static final String RAINBOW_TARGET_PROPERTIES_OUTPUT="rainbow-target-properties";
	public static final String RAINBOW_GENERRATED_SOURCE_OUTPUT="rainbow-generated-source";
	
	@Override
	public Set<OutputConfiguration> getOutputConfigurations() {
		OutputConfiguration defaultOutput = new OutputConfiguration(RAINBOW_GENERRATED_SOURCE_OUTPUT);
	    defaultOutput.setDescription("Output Folder");
	    defaultOutput.setOutputDirectory("./src/main/java-gen");
	    defaultOutput.setOverrideExistingResources(true);
	    defaultOutput.setCreateOutputDirectory(true);
	    defaultOutput.setCleanUpDerivedResources(true);
	    defaultOutput.setSetDerivedProperty(true);
	    
	    OutputConfiguration doc = new OutputConfiguration(IFileSystemAccess.DEFAULT_OUTPUT);
	    doc.setDescription("Output Folder");
	    doc.setOutputDirectory("./src/main/java-gen");
	    doc.setOverrideExistingResources(true);
	    doc.setCreateOutputDirectory(true);
	    doc.setCleanUpDerivedResources(true);
	    doc.setSetDerivedProperty(true);
	    
	    OutputConfiguration propertyOutput = new OutputConfiguration(RAINBOW_TARGET_PROPERTIES_OUTPUT);
	    propertyOutput.setDescription("Rainbow target property output folder");
	    propertyOutput.setOutputDirectory("./src/main/resources/generated");
	    propertyOutput.setOverrideExistingResources(true);
	    propertyOutput.setCreateOutputDirectory(true);
	    propertyOutput.setCleanUpDerivedResources(true);
	    propertyOutput.setSetDerivedProperty(true);
	    
	    return new HashSet<>(Arrays.asList(new OutputConfiguration []{defaultOutput, propertyOutput}));
	    
	}

}
