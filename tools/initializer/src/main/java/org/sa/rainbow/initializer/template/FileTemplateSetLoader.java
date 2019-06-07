package org.sa.rainbow.initializer.template;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Template;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.template.models.Metadata;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of TemplateSetLoaders that loads TemplateSet from local files.
 *
 * @author Jiahui Feng
 * @since 1.0
 */
public class FileTemplateSetLoader extends TemplateSetLoader {
    /**
     * The root of the template directory where all required files can be found.
     */
    private File root;

    /**
     * Constructs a FileTemplateSetLoader from a root directory.
     *
     * @param root the root directory where metadata and templates can be found.
     * @throws IOException when metadata or templates cannot be read or parsed.
     */
    public FileTemplateSetLoader(File root) throws IOException {
        super();
        this.root = root;
        configuration.setTemplateLoader(new FileTemplateLoader(root));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Template loadTemplate(String path) throws IOException {
        return configuration.getTemplate(path + TEMPLATE_EXTENSION);
    }

    /**
     * Loads the metadata from the root directory.
     *
     * @return the parsed Metadata
     * @throws IOException if metadata cannot be read or parsed.
     */
    private Metadata loadMetadata() throws IOException {
        Metadata metadata;
        try (Reader reader = new BufferedReader(new FileReader(new File(root, METADATA_FILENAME)))) {
            metadata = loadMetadata(reader);
        }
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemplateSet load() throws IOException {
        Metadata metadata = loadMetadata();
        Map<String, Template> templates = new HashMap<>();
        for (String file : metadata.getFiles()) {
            templates.put(file, loadTemplate(file));
        }
        return new TemplateSet(templates, metadata.getVariables());
    }

    public File getRoot() {
        return root;
    }
}
