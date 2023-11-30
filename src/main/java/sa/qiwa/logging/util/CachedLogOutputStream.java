package sa.qiwa.logging.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.*;

public class CachedLogOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream copy = new ByteArrayOutputStream();
    private ServletOutputStream outputStream;

    public CachedLogOutputStream(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public String getCopy() {
        return new String(copy.toByteArray());
    }

    @Override
    public boolean isReady() {
        return outputStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        outputStream.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        copy.write(b);
    }
}