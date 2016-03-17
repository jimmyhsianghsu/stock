package ch.web.captcha;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import ch.web.captcha.CaptchaEntity;
public class CaptchaCaptureFilter extends OncePerRequestFilter {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CaptchaCaptureFilter.class);
	private String defaultFilterProcessesUrl;
	private String defaultFailureUrl;
	public void setDefaultFilterProcessesUrl(String defaultFilterProcessesUrl) {
		this.defaultFilterProcessesUrl = defaultFilterProcessesUrl;
	}
	public void setDefaultFailureUrl(String defaultFailureUrl){
		this.defaultFailureUrl = defaultFailureUrl;
	}
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
		LOG.info(getParameterMap(req));
		Authentication auth = SecurityContextHolder.getContext().getAuthentication(); //
		if (req.getRequestURI().endsWith(defaultFilterProcessesUrl)){
			CaptchaEntity captchaEntity = (CaptchaEntity) req.getSession().getAttribute("captchaEntity");
			if (captchaEntity == null || !captchaEntity.getAnswer().equals(req.getParameter("verifyCode"))){
				res.sendRedirect(req.getContextPath() + defaultFailureUrl);
				if (auth != null) auth.setAuthenticated(false);
				return;
			}
		}
		if (auth != null){
			LOG.info("auth: " + auth.getName() + ", Granted Authorities: " + auth.getAuthorities());
		}
		chain.doFilter(req, res);
	}
	private List<Map<String, String>> getParameterMap(HttpServletRequest req){
		Map<String, String> reqMap = new LinkedHashMap<String, String>();
		reqMap.put("requestURI", req.getRequestURI());
		Enumeration<String> paramNames = req.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			reqMap.put(paramName, req.getParameter(paramName));
		}
		Map<String, String> sesMap = new LinkedHashMap<String, String>();
		Enumeration<String> attrNames = req.getSession().getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			sesMap.put(attrName, req.getSession().getAttribute(attrName).toString());
		}
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(reqMap);
		list.add(sesMap);
		return list;
	}
	private void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException{
		new SimpleUrlAuthenticationFailureHandler(defaultFailureUrl).onAuthenticationFailure(req, res, new BadCredentialsException("GG"));
	}
}