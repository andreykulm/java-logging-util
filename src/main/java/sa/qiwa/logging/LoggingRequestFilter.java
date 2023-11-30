package sa.qiwa.logging;

import org.springframework.web.filter.OncePerRequestFilter;
import sa.qiwa.logging.util.CachedLogHttpServletRequest;
import sa.qiwa.logging.util.CachedLogHttpServletResponse;
import sa.qiwa.logging.util.LoggingUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

public class LoggingRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        CachedLogHttpServletRequest cachedHttpServletRequest = new CachedLogHttpServletRequest(request);
        CachedLogHttpServletResponse cachedHttpServletResponse = new CachedLogHttpServletResponse(response);
        Instant started = Instant.now();
        filterChain.doFilter(cachedHttpServletRequest, cachedHttpServletResponse);
        LoggingUtil.logAppExchange(started, cachedHttpServletRequest, cachedHttpServletResponse);
    }
}
