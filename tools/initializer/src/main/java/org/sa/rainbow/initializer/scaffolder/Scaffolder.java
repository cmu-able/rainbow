package org.sa.rainbow.initializer.scaffolder;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.sa.rainbow.initializer.models.TemplateSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * The scaffolder loads template and configuration, and generates resulting work directory.
 *
 * @author Jiahui Feng
 * @since 1.0
 */
public class Scaffolder {
    /**
     * The base directory for the scaffolder to create directories and files.
     * Defaults to current working directory.
     */
    private Path baseDirectory = Paths.get(".");
    /**
     * The template set. Should be correctly loaded.
     */
    private TemplateSet templateSet;
    /**
     * A configuration is simply a K-V mapping of each variable to its value.
     */
    private Map<String, Object> configuration;

    /**
     * Constructs a scaffolder instance with given TemplateSet and corresponding configuration.
     *
     * @param templateSet   the template set
     * @param configuration the corresponding configuration
     */
    public Scaffolder(TemplateSet templateSet, Map<String, Object> configuration) {
        this.templateSet = templateSet;
        this.configuration = configuration;
    }

    public TemplateSet getTemplateSet() {
        return templateSet;
    }

    public void setTemplateSet(TemplateSet templateSet) {
        this.templateSet = templateSet;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    /**
     * Generate a single file using template, and write its content to the given path.
     * The given path must be writable.
     *
     * @param path     the path to write to
     * @param template the template to process
     * @throws IOException       if the output path cannot be written to
     * @throws TemplateException if we cannot process the template
     */
    private void writeFile(Path path, Template template) throws IOException, TemplateException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            template.process(configuration, writer);
        }
    }

    /**
     * Generates the working directory with template set and configuration.
     *
     * @throws IOException          if the working directory cannot be written to
     * @throws ScaffoldingException if a variable is missing or a template is invalid
     */
    public void scaffold() throws IOException, ScaffoldingException {
        try {
            for (Map.Entry<String, Template> entry : templateSet.getTemplates().entrySet()) {
                String s = entry.getKey();
                Template template = entry.getValue();

                // Found out where to put this new file.
                Path path = baseDirectory.resolve(s);

                // Get the parent directory so that we can create it if we need to.
                Path parentDirectory = path.getParent();

                // Try create required directories first.
                Files.createDirectories(parentDirectory);

                // Finally, generate the file with the template.
                writeFile(path, template);
            }
        } catch (TemplateException e) {
            throw new ScaffoldingException("error processing template", e);
        }
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
}
