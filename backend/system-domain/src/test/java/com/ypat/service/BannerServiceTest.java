package com.ypat.service;

import com.ypat.BannerQo;
import com.ypat.SysException;
import com.ypat.entity.Banner;
import com.ypat.repository.BannerRepository;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BannerServiceTest {

    @Test
    public void saveClearsJumpDetailsWhenJumpDisabled() {
        BannerService service = new BannerService();
        SavedBanner saved = new SavedBanner();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(saved));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("2");
        qo.setJumptype("web");
        qo.setJumpurl("https://example.com/activity");

        service.save(qo);

        assertEquals("0", saved.banner.getJumpflag());
        assertNull(saved.banner.getJumptype());
        assertNull(saved.banner.getJumpurl());
    }

    @Test
    public void saveDefaultsBlankJumpflagToZero() {
        BannerService service = new BannerService();
        SavedBanner saved = new SavedBanner();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(saved));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("");
        qo.setJumptype("web");
        qo.setJumpurl("https://example.com/activity");

        service.save(qo);

        assertEquals("0", saved.banner.getJumpflag());
        assertNull(saved.banner.getJumptype());
        assertNull(saved.banner.getJumpurl());
    }

    @Test(expected = SysException.class)
    public void saveRejectsInvalidWebJumpUrl() {
        BannerService service = new BannerService();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(new SavedBanner()));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("web");
        qo.setJumpurl("javascript:alert(1)");

        service.save(qo);
    }

    @Test(expected = SysException.class)
    public void saveRejectsBlankJumpUrl() {
        BannerService service = new BannerService();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(new SavedBanner()));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("miniapp");
        qo.setJumpurl("");

        service.save(qo);
    }

    @Test(expected = SysException.class)
    public void saveRejectsTooLongJumpUrl() {
        BannerService service = new BannerService();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(new SavedBanner()));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("web");
        qo.setJumpurl(repeat("a", 501));

        service.save(qo);
    }

    @Test(expected = SysException.class)
    public void saveRejectsInvalidMiniappPath() {
        BannerService service = new BannerService();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(new SavedBanner()));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("miniapp");
        qo.setJumpurl("/work/index");

        service.save(qo);
    }

    @Test(expected = SysException.class)
    public void saveRejectsEnabledJumpWithoutType() {
        BannerService service = new BannerService();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(new SavedBanner()));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("");
        qo.setJumpurl("/pages/work/index");

        service.save(qo);
    }

    @Test
    public void saveAcceptsMiniappPath() {
        BannerService service = new BannerService();
        SavedBanner saved = new SavedBanner();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(saved));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("miniapp");
        qo.setJumpurl("/pages-sub/work/detail?id=1");

        service.save(qo);

        assertEquals("1", saved.banner.getJumpflag());
        assertEquals("miniapp", saved.banner.getJumptype());
        assertEquals("/pages-sub/work/detail?id=1", saved.banner.getJumpurl());
    }

    @Test
    public void saveAcceptsUppercaseHttpsWebJumpUrl() {
        BannerService service = new BannerService();
        SavedBanner saved = new SavedBanner();
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(saved));

        BannerQo qo = new BannerQo();
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");
        qo.setJumpflag("1");
        qo.setJumptype("web");
        qo.setJumpurl("HTTPS://example.com/activity");

        service.save(qo);

        assertEquals("1", saved.banner.getJumpflag());
        assertEquals("web", saved.banner.getJumptype());
        assertEquals("HTTPS://example.com/activity", saved.banner.getJumpurl());
    }

    @Test
    public void upDownNormalizesDirtyDisabledJumpDataBeforeSave() {
        BannerService service = new BannerService();
        SavedBanner saved = new SavedBanner();
        Banner existing = new Banner();
        existing.setId(7L);
        existing.setTitle("banner");
        existing.setImgpath("/img/banner.png");
        existing.setJumpflag("0");
        existing.setJumptype("web");
        existing.setJumpurl("https://example.com/activity");
        ReflectionTestUtils.setField(service, "bannerRepository", bannerRepository(saved, existing));

        BannerQo qo = new BannerQo();
        qo.setId(7L);
        qo.setStatus("1");

        service.upDown(qo);

        assertEquals("1", saved.banner.getStatus());
        assertEquals("0", saved.banner.getJumpflag());
        assertNull(saved.banner.getJumptype());
        assertNull(saved.banner.getJumpurl());
    }

    private static BannerRepository bannerRepository(final SavedBanner saved) {
        return bannerRepository(saved, null);
    }

    private static BannerRepository bannerRepository(final SavedBanner saved, final Banner existing) {
        return proxy(BannerRepository.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("findById".equals(method.getName())) {
                    return existing;
                }
                if ("save".equals(method.getName())) {
                    saved.banner = (Banner) args[0];
                    return args[0];
                }
                return defaultValue(method.getReturnType());
            }
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (Object.class.equals(method.getDeclaringClass())) {
                    if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                    if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                    if ("equals".equals(method.getName())) return proxy == args[0];
                }
                return handler.invoke(proxy, method, args);
            }
        }));
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (boolean.class.equals(type)) return false;
        if (int.class.equals(type)) return 0;
        if (long.class.equals(type)) return 0L;
        return null;
    }

    private static String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private static class SavedBanner {
        private Banner banner;
    }
}
