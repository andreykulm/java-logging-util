package sa.qiwa.logging.util;

import org.springframework.util.StreamUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class CachedLogHttpServletRequest extends HttpServletRequestWrapper {

	private final byte[] cachedPayload;

	public CachedLogHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		InputStream requestInputStream = request.getInputStream();
		this.cachedPayload = StreamUtils.copyToByteArray(requestInputStream);
	}

	@Override
	public ServletInputStream getInputStream() {
		return new CachedLogServletInputStream(this.cachedPayload);
	}

	@Override
	public BufferedReader getReader() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedPayload);
		return new BufferedReader(new InputStreamReader(byteArrayInputStream));
	}
}

