/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.cmu.rainbow_ui.storage;

import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.jconv.DefaultTypelibJavaConverter;
import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.cs.able.typelib.parser.DefaultTypelibParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.model.acme.eseb.AcmeConverter;

/**
 * Acme Model serializer.
 *
 * <p>
 * Performs conversion of Acme Model instance to/from string serialized representation.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeModelSerializer {

    /* Typelib scope */
    private final PrimitiveScope scope = new PrimitiveScope();
    /* Default converter for standard types */
    private final DefaultTypelibJavaConverter converter = DefaultTypelibJavaConverter.make(scope);
    /* Encoding to convert data values to strings and back */
    private final DefaultTextEncoding encoding = new DefaultTextEncoding(scope);

    /**
     * Default constructor. Sets up conversion.
     */
    public AcmeModelSerializer() {
        try {
            /* Add Rainbow Model conversion rule */
            converter.add(new AcmeConverter(scope));
            DefaultTypelibParser parser = DefaultTypelibParser.make();
            TypelibParsingContext context = new TypelibParsingContext(scope, scope);
            parser.parse(
                    new ParsecFileReader()
                    .read_memory("struct rainbow_model {string name; string type; string source; string cls; string system_name; string serialization; string additional_info;}"),
                    context);
        } catch (LocalizedParseException ex) {
            Logger.getLogger(AcmeModelSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Convert model instance to string.
     *
     * @param instance model to convert
     * @return serialized model as string
     */
    public String serialize(IModelInstance<IAcmeSystem> instance) {
        String serial = null;
        try {
            DataValue value = converter.from_java(instance, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            encoding.encode(value, new DataOutputStream(baos));
            serial = baos.toString();
        } catch (ValueConversionException | IOException ex) {
            Logger.getLogger(AcmeModelSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return serial;
    }

    /**
     * Convert serialized model to model instance.
     *
     * @param serial serialized model as string
     * @return model instance
     */
    public IModelInstance<IAcmeSystem> deserialize(String serial) {
        IModelInstance<IAcmeSystem> instance = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(serial.getBytes());
        DataValue value;
        try {
            value = encoding.decode(new DataInputStream(bais), scope);
            instance = converter.to_java(value, null);
        } catch (IOException | InvalidEncodingException | ValueConversionException ex) {
            Logger.getLogger(AcmeModelSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return instance;
    }

}
