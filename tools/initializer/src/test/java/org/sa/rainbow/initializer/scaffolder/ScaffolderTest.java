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
import java.util.*;

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

        // A simple mapping for testing
        loader.putTemplate("mapping.yml.ftl", "hello.[=name].txt: example.txt.ftl\n".getBytes());
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
        Map<String, Template> templates = new HashMap<>();
        templates.put("example.txt.ftl", configuration.getTemplate("example.txt.ftl"));
        templates.put("mapping.yml.ftl", configuration.getTemplate("mapping.yml.ftl"));
        List<Variable> variables = Collections.singletonList(new Variable("name"));
        TemplateSet templateSet = new TemplateSet(templates, variables);
        Map<String, Object> configuration = Collections.singletonMap("name", "world");

        Scaffolder scaffolder = new Scaffolder(templateSet, configuration);
        scaffolder.setBaseDirectory(baseDirectory);
        scaffolder.scaffold();

        // read the content of expected files, and assert its content.
        assertEquals("Hello, world!\n", new String(Files.readAllBytes(baseDirectory.resolve("hello.world.txt"))));
    }
}