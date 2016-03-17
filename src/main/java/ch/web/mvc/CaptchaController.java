package ch.web.mvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import ch.web.captcha.CaptchaEntity;
import ch.web.captcha.CaptchaService;
@Controller
@SessionAttributes("captchaEntity")
public class CaptchaController {
	@Autowired
	private CaptchaService captchaService;
	@ModelAttribute("captchaEntity")
	public CaptchaEntity getCaptchaEntity(){
		return new CaptchaEntity();
	}
	@RequestMapping(value = "/captcha.jpg", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getCaptchaJpg(Model model){
		CaptchaEntity captchaEntity = (CaptchaEntity) model.asMap().get("captchaEntity");
		if (captchaEntity != null){
			CaptchaEntity newCaptcha = captchaService.getCaptcha();
			if (newCaptcha != null){
				captchaEntity.setAnswer(newCaptcha.getAnswer());
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_JPEG);
				return new ResponseEntity<byte[]>(newCaptcha.getImgBytes(), headers, HttpStatus.CREATED);
			}
		}
		return null;
	}
}