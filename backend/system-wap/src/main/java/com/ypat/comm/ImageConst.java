package com.ypat.comm;

import java.awt.*;

public interface ImageConst {
    public static final String IMAGE_TYPE = "jpg";
    /** 水印文字内容 */
    public static final String MARK_TEXT = "91去拍";
    /** 水印文字类型 */
    public static final String FONT_NAME = "微软雅黑";
    /** 水印文字样式 */
    public static final int FONT_STYLE = Font.BOLD;
    /** 水印文字大小 */
    public static final int FONT_SIZE= 50;// 单位:像素
    /** 水印文字颜色 */
    public static final Color FONT_COLOR= Color.white;
    /** 水印文字位置X轴 */
    public static final int X = 10;
    /** 水印文字位置Y轴 */
    public static final int Y = 10;
    /** 水印文字透明度*/
    public static final float ALPHA = 0.6F;
    /** 水印图片*/
    public static final String LOGO = "logo.jpg";
}
