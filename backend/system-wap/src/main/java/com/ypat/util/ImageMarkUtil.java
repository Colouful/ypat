package com.ypat.util;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.comm.ImageConst;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 图片水印服务类，添加图片水印
 *
 */
@Component
public class ImageMarkUtil implements ImageConst {

    private static Logger logger = Logger.getLogger(ImageMarkUtil.class);

    public InputStream waterMake(InputStream imageFile) {
        ByteArrayInputStream inputStream = null;
        try {
            Image image = ImageIO.read(imageFile);
            int width = image.getWidth(null);// 原图宽度
            int height = image.getHeight(null);// 原图高度

            // 创建图片缓存对象
            BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            // 创建绘绘图工具对象
            Graphics2D g = bufferedImage.createGraphics();
            // 使用绘图工具将原图绘制到缓存图片对象
            g.drawImage(image, 0, 0, width,height,null);
            // 读取Logo图片
            InputStream logo = ImageMarkUtil.class.getClassLoader().getResourceAsStream("conf/"+LOGO);
            Image imageLogo = ImageIO.read(logo);
            // 获取Logo图片的宽度和高度
            int markWidth = imageLogo.getWidth(null);
            int markHeight = imageLogo.getHeight(null);
            // 原图和Logo图片的高度和宽度之差
            int widthDiff = width - markWidth;
            int heightDiff = height - markHeight;
            int x = widthDiff - X;
            int y = heightDiff - Y;

            // 设置水印透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, ALPHA));
            // 添加水印
            g.drawImage(imageLogo, x, y, null);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, IMAGE_TYPE, out);
            inputStream = new ByteArrayInputStream(out.toByteArray());
//            OutputStream os = new FileOutputStream("d://temp//test.jpg");
//            JPEGImageEncoder en = JPEGCodec.createJPEGEncoder(os);
//            en.encode(bufferedImage);
        } catch (Exception e) {
            logger.error("加水印失败："+e);
            throw new SysException(ResponseCode.FAIL_MARK);
        }
        return inputStream;
    }

    public InputStream waterMakeFont(InputStream imageFile) {
        ByteArrayInputStream inputStream = null;
        try {
            Image image = ImageIO.read(imageFile);
            int width = image.getWidth(null);// 原图宽度
            int height = image.getHeight(null);// 原图高度

            // 创建图片缓存对象
            BufferedImage bufferedImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            // 创建绘绘图工具对象
            Graphics2D g = bufferedImage.createGraphics();
            // 使用绘图工具将原图绘制到缓存图片对象
            g.drawImage(image, 0, 0, width,height,null);
            // 设置水印文字字体信息
            g.setFont(new Font(FONT_NAME,FONT_STYLE,FONT_SIZE));
            // 设置水印文字颜色
            g.setColor(FONT_COLOR);

            int markWidth = FONT_SIZE * getTextLength(MARK_TEXT);
            int markHeight = FONT_SIZE;
            // 水印的高度和宽度之差
            int widthDiff = width - markWidth;
            int heightDiff = height - markHeight;
            int x = widthDiff - X-110;
            int y = heightDiff - Y-110;
            // 设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, ALPHA));
            // 添加水印
            g.drawString(MARK_TEXT, x, y + FONT_SIZE);
            g.dispose();

//            OutputStream os = new FileOutputStream("d://temp//test.jpg");
//            JPEGImageEncoder en = JPEGCodec.createJPEGEncoder(os);
//            en.encode(bufferedImage);
        } catch (Exception e) {
            logger.error("加水印失败："+e);
            throw new SysException(ResponseCode.FAIL_MARK);
        }
        return inputStream;
    }

    /**
     * 功能：获取文本长度。汉字为1:1，英文和数字为2:1
     */
    private int getTextLength(String text){
        int length = text.length();
        for(int i = 0 ; i < text.length(); i++){
            String s = String.valueOf(text.charAt(i));
            if(s.getBytes().length > 1){
                length++;
            }
        }
        length = length % 2 == 0 ? length / 2 : length / 2 + 1;
        return length;
    }

}