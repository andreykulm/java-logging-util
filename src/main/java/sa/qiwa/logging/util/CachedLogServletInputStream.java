package sa.qiwa.logging.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class CachedLogServletInputStream extends ServletInputStream {

	private final InputStream cachedInputStream;

	public CachedLogServletInputStream(byte[] cachedBody) {
		this.cachedInputStream = new ByteArrayInputStream(cachedBody);
	}

	@Override
	public boolean isFinished() {
		try {
			return cachedInputStream.available() == 0;
		} catch (IOException exp) {
			log.error(exp.getMessage());
		}
		return false;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read() throws IOException {
		return cachedInputStream.read();
	}
}
