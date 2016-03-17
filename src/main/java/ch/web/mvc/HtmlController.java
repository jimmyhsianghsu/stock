package ch.web.mvc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
public class HtmlController {
	@Autowired private HttpServletRequest request;
	@Autowired private HttpServletResponse response;
	@RequestMapping(value = "/stockNow.html", method = RequestMethod.GET)
	public String stock(){
		response.setCharacterEncoding("UTF-8");
		return "stockNow.html";
	}
	@RequestMapping(value = "/dbm.html", method = RequestMethod.GET)
	public String dbm(){
		response.setCharacterEncoding("UTF-8");
		return "dbm.html";
	}
	@RequestMapping(value = "/dbm1.html", method = RequestMethod.GET)
	public String dbm1(){
		response.setCharacterEncoding("UTF-8");
		return "dbm1.html";
	}
	@RequestMapping(value = "/admin.html", method = RequestMethod.GET)
	public String admin(){
		response.setCharacterEncoding("UTF-8");
		return "admin.html";
	}
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(@RequestParam(value = "targetUrl", required = false) String targetUrl,
			@RequestParam(value = "success", required = false) String success,
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			@RequestParam(value = "denied", required = false) String denied){
		if (success != null){
			return "redirect:" + stock();
		} else if (targetUrl != null || error != null || logout != null){
			return "login.html";
		} else if (denied != null){
			return "redirect:login?err403";
		} else {
			targetUrl = getTargetUrl();
			if (targetUrl == null){
				return "login.html";
			} else {
				return "redirect:login?targetUrl=" + targetUrl;
			}
		}
	}
	private String getTargetUrl(){
		String targetUrl = null;
		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, null);
		if (savedRequest != null){
			targetUrl = savedRequest.getRedirectUrl();
			if (targetUrl != null){
				String path = request.getContextPath() + '/';
				targetUrl = targetUrl.substring(targetUrl.indexOf(path) + path.length());
			}
		}
		return targetUrl;
	}
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(){return "redirect:login";}
}