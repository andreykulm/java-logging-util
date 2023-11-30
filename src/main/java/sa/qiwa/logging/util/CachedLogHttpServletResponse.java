package sa.qiwa.logging.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

public class CachedLogHttpServletResponse extends HttpServletResponseWrapper {

    private final CachedLogOutputStream cachedLogOutputStream;

    public CachedLogHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        cachedLogOutputStream = new CachedLogOutputStream(response.getOutputStream());
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return cachedLogOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(cachedLogOutputStream);
    }

    public CachedLogOutputStream getCached() {
        return cachedLogOutputStream;
    }
}

