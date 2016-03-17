package ch.web.captcha;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import jj.play.ns.nl.captcha.Captcha;
import jj.play.ns.nl.captcha.backgrounds.GradiatedBackgroundProducer;
import jj.play.ns.nl.captcha.gimpy.BlockGimpyRenderer;
import jj.play.ns.nl.captcha.gimpy.RippleGimpyRenderer;
import jj.play.ns.nl.captcha.noise.CurvedLineNoiseProducer;
@Service
public class CaptchaServiceImpl implements CaptchaService {
	@Override
	public CaptchaEntity getCaptcha() {
		Captcha captcha = new Captcha.Builder(200, 50).addText()
				.addBackground(new GradiatedBackgroundProducer())
				.addNoise(new CurvedLineNoiseProducer()).addNoise(new CurvedLineNoiseProducer())
				.gimp(new RippleGimpyRenderer()).gimp(new RippleGimpyRenderer()).gimp(new BlockGimpyRenderer())
				.build();
		byte[] imgBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(captcha.getImage(), "jpg", baos);
			baos.flush();
			imgBytes = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new CaptchaEntity(captcha.getAnswer(), imgBytes);
	}
}