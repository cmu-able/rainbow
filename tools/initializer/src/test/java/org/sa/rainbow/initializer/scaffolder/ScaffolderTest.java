package org.sa.rainbow.initializer.scaffolder;

import freemarker.cache.ByteArrayTemplateLoader;
import freemarker.core.PlainTextOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.models.Variable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Scaffolder test cases.
 *
 * @author Jiahui Feng
 */
public class ScaffolderTest {

    private Path baseDirectory;
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        baseDirectory = Files.createTempDirectory("scaffolder");
        this.configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        configuration.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        configuration.setOutputFormat(PlainTextOutputFormat.INSTANCE);
        ByteArrayTemplateLoader loader = new ByteArrayTemplateLoader();

        // A simple template for testing
        loader.putTemplate("example.txt.ftl", "Hello, [=name]!\n".getBytes());

        configuration.setTemplateLoader(loader);
    }

    @After
    public void tearDown() throws Exception {
        // delete everything under baseDirectory
        Files.walk(baseDirectory).sorted(Comparator.reverseOrder()).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void scaffold() throws Exception {
        Map<String, Template> templates = Collections.singletonMap("path/to/example.txt", configuration.getTemplate("example.txt.ftl"));
        List<Variable> variables = Collections.singletonList(new Variable("name"));
        TemplateSet templateSet = new TemplateSet(templates, variables);
        Map<String, String> configuration = Collections.singletonMap("name", "world");

        Scaffolder scaffolder = new Scaffolder(templateSet, configuration);
        scaffolder.setBaseDirectory(baseDirectory);
        scaffolder.scaffold();

        // read the content of expected files, and assert its content.
        assertEquals("Hello, world!\n", new String(Files.readAllBytes(baseDirectory.resolve("path/to/example.txt"))));
    }
}