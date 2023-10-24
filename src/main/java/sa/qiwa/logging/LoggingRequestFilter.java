package sa.qiwa.logging;

import org.springframework.web.filter.OncePerRequestFilter;
import sa.qiwa.logging.util.CachedLogHttpServletRequest;
import sa.qiwa.logging.util.LoggingUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoggingRequestFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		CachedLogHttpServletRequest cachedHttpServletRequest = new CachedLogHttpServletRequest(request);
		LoggingUtil.logServerRequest(cachedHttpServletRequest);
		filterChain.doFilter(cachedHttpServletRequest, response);
		LoggingUtil.logServerResponse(cachedHttpServletRequest, response);
	}
}
