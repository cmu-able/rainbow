package org.sa.rainbow.initializer.template;

import freemarker.template.Template;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.models.Variable;
import org.sa.rainbow.initializer.template.models.Metadata;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class FileTemplateSetLoaderTest {
    private Path tempPath;

    /**
     * Creates a simple metadata file for testing.
     *
     * @param metadataFile the metadata file.
     * @throws IOException when files cannot be written.
     */
    private void createMetadata(File metadataFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile))) {
            Metadata metadata = new Metadata();
            metadata.setVariables(Collections.singletonList(new Variable("name")));
            metadata.setFiles(Collections.singletonList("simple/simple.txt"));
            Yaml yaml = new Yaml();
            yaml.dump(metadata, writer);
        }
    }

    /**
     * Creates a simple template for testing.
     *
     * @param simpleFile the simple template.
     * @throws IOException when files cannot be written.
     */
    private void createSimpleTemplate(File simpleFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(simpleFile))) {
            writer.write("Hello, [=name]!");
            writer.newLine();
        }
    }

    @Before
    public void setUp() throws Exception {
        tempPath = Files.createTempDirectory("template");
        Path simpleDirectory = tempPath.resolve("simple");
        Files.createDirectory(simpleDirectory);

        createMetadata(tempPath.resolve("metadata.yml").toFile());
        createSimpleTemplate(simpleDirectory.resolve("simple.txt.tfl").toFile());
    }

    @After
    public void tearDown() throws Exception {
        // delete everything under tempPath
        Files.walk(tempPath).sorted(Comparator.reverseOrder()).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void loadTemplate() throws Exception {
        TemplateSetLoader loader = new FileTemplateSetLoader(tempPath.toFile());
        Template template = loader.loadTemplate("simple/simple.txt");
        StringWriter writer = new StringWriter();
        template.process(Collections.singletonMap("name", "world"), writer);
        writer.close();
        assertEquals("Hello, world!\n", writer.toString());
    }

    @Test
    public void load() throws Exception {
        TemplateSetLoader loader = new FileTemplateSetLoader(tempPath.toFile());
        TemplateSet templateSet = loader.load();
        assertEquals(1, templateSet.getVariables().size());
        assertEquals("name", templateSet.getVariables().get(0).getName());
    }
}
