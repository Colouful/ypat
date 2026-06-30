package com.ypat.util;

/**
 * 邀请码编解码：以 base36(userId) + "IV" 前缀生成短码，作为 recmobile 的安全替代。
 *
 * 设计目标：
 * - 避免在分享链接里直接暴露邀请人手机号（旧版 `recmobile=138xxxx` 的安全隐患）
 * - 可逆：解码即可拿到 inviter user.id，无需额外查找表
 * - 仅做混淆，不是加密；前端可推断但不能拿到 mobile/openid
 *
 * 不要把这个方法用于任何鉴权 / 防伪场景。
 */
public final class InviteCodeCodec {

    private static final String PREFIX = "IV";

    private InviteCodeCodec() {
    }

    public static String encode(Long userId) {
        if (userId == null || userId <= 0) return null;
        return PREFIX + Long.toString(userId, 36).toUpperCase();
    }

    public static Long decode(String inviteCode) {
        if (inviteCode == null) return null;
        String trimmed = inviteCode.trim().toUpperCase();
        if (!trimmed.startsWith(PREFIX) || trimmed.length() <= PREFIX.length()) return null;
        String body = trimmed.substring(PREFIX.length());
        try {
            long id = Long.parseLong(body, 36);
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
