package ch.web.captcha;
public class CaptchaEntity {
	private String answer;
	private byte[] imgBytes;
	public CaptchaEntity(){}
	public CaptchaEntity(String answer, byte[] imgBytes){
		this.answer = answer;
		this.imgBytes = imgBytes;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public byte[] getImgBytes() {
		return imgBytes;
	}
	@Override
	public String toString() {
		return answer;
	}
}