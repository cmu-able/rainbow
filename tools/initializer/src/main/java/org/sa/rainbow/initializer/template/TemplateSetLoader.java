package org.sa.rainbow.initializer.template;

import freemarker.core.PlainTextOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.template.models.Metadata;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

/**
 * Abstract class of a loader that loads a TemplateSet from an external source.
 *
 * @author Jiahui Feng
 * @since 1.0
 */
public abstract class TemplateSetLoader {
    /**
     * The name of metadata file.
     */
    protected static final String METADATA_FILENAME = "metadata.yml";
    /**
     * The FreeMarker configuration.
     */
    protected Configuration configuration;

    /**
     * Constructs the loader, setting required configuration of FreeMarker.
     */
    protected TemplateSetLoader() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        configuration.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        configuration.setOutputFormat(PlainTextOutputFormat.INSTANCE);
    }

    /**
     * Loads a single template from a given path.
     *
     * @param templatePath path of the template, as specified in metadata, without '.ftl' extension.
     * @return a FreeMarker template instance.
     * @throws IOException when the template cannot be read or parsed.
     */
    protected abstract Template loadTemplate(String templatePath) throws IOException;

    /**
     * Loads the metadata from a reader.
     *
     * @param reader the reader to read metadata from.
     * @return the parsed metadata.
     */
    protected Metadata loadMetadata(Reader reader) throws InvalidMetadataException {
        Yaml yaml = new Yaml();
        Metadata metadata = yaml.load(reader);
        if (metadata.getFiles() == null) {
            throw new InvalidMetadataException("field 'files' is required in metadata.");
        }
        if (metadata.getVariables() == null) {
            metadata.setVariables(Collections.emptyList());
        }
        return metadata;
    }

    /**
     * Loads the TemplateSet.
     *
     * @return the TemplateSet.
     * @throws IOException when templates cannot be read, parsed, or validated.
     */
    public abstract TemplateSet load() throws IOException;
}
