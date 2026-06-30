package com.ypat.service;

import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理端图片验证码服务。
 *
 * <p>由于 system-wap 采用无状态（STATELESS）会话策略，无法使用 HttpSession 存储验证码，
 * 因此使用 {@link ConcurrentHashMap} + UUID 关联 + TTL 过期清理方案。</p>
 */
@Service
public class AdminCaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCaptchaService.class);

    /** 验证码缓存：captchaId -> 验证码文本 */
    private final ConcurrentHashMap<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    /** 验证码有效期 5 分钟 */
    private static final long CAPTCHA_TTL_MS = 5 * 60 * 1000L;

    /** 验证码图片宽度 */
    private static final int WIDTH = 120;

    /** 验证码图片高度 */
    private static final int HEIGHT = 40;

    /** 验证码字符数 */
    private static final int CODE_LENGTH = 4;

    /** 验证码可选字符 */
    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789".toCharArray();

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成验证码图片。
     *
     * @return ResponseApiBody 包含 captchaId 和 base64 编码的图片
     */
    public ResponseApiBody generate() {
        cleanExpired();

        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();

        captchaStore.put(captchaId, new CaptchaEntry(code, System.currentTimeMillis()));

        String base64Image = generateImageBase64(code);

        java.util.Map<String, String> res = new java.util.HashMap<>(4);
        res.put("captchaId", captchaId);
        res.put("img", "data:image/jpeg;base64," + base64Image);

        return ResponseApiBody.success(res);
    }

    /**
     * 校验验证码。
     *
     * @param captchaId 验证码 ID
     * @param inputCode 用户输入的验证码
     * @return true 校验通过
     * @throws SysException 验证码失效或错误时抛出
     */
    public boolean verify(String captchaId, String inputCode) {
        if (captchaId == null || captchaId.isEmpty() || inputCode == null || inputCode.isEmpty()) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "验证码不能为空");
        }

        CaptchaEntry entry = captchaStore.get(captchaId);
        if (entry == null) {
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "验证码已失效，请重新获取");
        }

        long elapsed = System.currentTimeMillis() - entry.createTime;
        if (elapsed > CAPTCHA_TTL_MS) {
            captchaStore.remove(captchaId);
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "验证码已失效，请重新获取");
        }

        if (!entry.code.equalsIgnoreCase(inputCode)) {
            captchaStore.remove(captchaId);
            throw new SysException(ResponseCode.FAIL_PARA.getCode(), "验证码错误");
        }

        captchaStore.remove(captchaId);
        return true;
    }

    /**
     * 生成随机验证码文本。
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片的 Base64 编码。
     */
    private String generateImageBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
                g.drawString(String.valueOf(code.charAt(i)), 8 + i * 28, 28);
            }

            // 干扰线
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
                g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            logger.error("生成验证码图片失败", e);
            throw new SysException(ResponseCode.FAIL_SER.getCode(), "验证码生成失败");
        } finally {
            g.dispose();
        }
    }

    /**
     * 清理过期验证码。
     */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, CaptchaEntry>> it = captchaStore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CaptchaEntry> entry = it.next();
            if (now - entry.getValue().createTime > CAPTCHA_TTL_MS) {
                it.remove();
            }
        }
    }

    /**
     * 验证码缓存条目。
     */
    private static class CaptchaEntry {
        final String code;
        final long createTime;

        CaptchaEntry(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
    }
}
