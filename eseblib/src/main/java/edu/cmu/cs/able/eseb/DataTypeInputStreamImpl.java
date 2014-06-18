package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Implementation of a data type input stream based on a data value encoding.
 */
public class DataTypeInputStreamImpl implements DataTypeInputStream {
    /**
     * Maximum allowed packet size.
     */
    private static final int  MAXIMUM_PACKET_SIZE = 20_000_000;

    /**
     * The input stream to read data from, <code>null</code> when stream is
     * closed.
     */
    private DataInputStream m_din;

    /**
     * Temporary data storage. It has {@link #MAXIMUM_PACKET_SIZE} bytes.
     */
    private byte[] m_buffer;

    /**
     * the data value encoding.
     */
    private DataValueEncoding m_encoding;

    /**
     * The primitive scope.
     */
    private PrimitiveScope m_pscope;

    /**
     * Creates a new input stream.
     * @param is the stream to read data from
     * @param enc the encoding to use
     * @param pscope the primitive scope to use to search for data types while
     * decoding
     */
    public DataTypeInputStreamImpl(InputStream is, DataValueEncoding enc,
            PrimitiveScope pscope) {
        Ensure.not_null(is);
        Ensure.not_null(enc);
        Ensure.not_null(pscope);

        m_din = new DataInputStream(is);
        m_buffer = new byte[MAXIMUM_PACKET_SIZE];
        m_encoding = enc;
        m_pscope = pscope;
    }

    @Override
    public BusData read() throws EOFException, IOException,
    InvalidEncodingException {
        if (m_din == null) throw new IllegalStateException("Stream is closed.");

        int size = m_din.readInt();
        if (size <= 0 || size > MAXIMUM_PACKET_SIZE) throw new IOException("Packet size is " + size + ".");

        int read = 0;
        while (read < size) {
            int r = m_din.read(m_buffer, read, size - read);
            if (r == -1) throw new EOFException();

            assert r >= 0;
            read += r;
        }

        try (ByteArrayInputStream input = new ByteArrayInputStream(m_buffer, 0,
                size);
                DataInputStream in = new DataInputStream(input)) {
            DataValue value = m_encoding.decode(in, m_pscope);
            return new BusData(value, m_buffer, size);
        } catch (Exception e) {
            return new BusData(m_buffer, size, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (m_din == null) return;

        m_din.close();
        m_din = null;
    }
}
