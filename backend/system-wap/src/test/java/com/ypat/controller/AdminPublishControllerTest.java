package com.ypat.controller;

import com.ypat.ArticleQo;
import com.ypat.BannerQo;
import com.ypat.ProductQo;
import com.ypat.ResponseApiBody;
import com.ypat.ResponseCode;
import com.ypat.service.ArticleServiceClient;
import com.ypat.service.BannerServiceClient;
import com.ypat.service.ProductServiceClient;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AdminPublishControllerTest {

    @Test
    public void bannerSaveAndUpDownTreatVoidServiceResponseAsSuccess() throws Exception {
        AdminBannerController controller = new AdminBannerController();
        RecordingBannerServiceClient client = new RecordingBannerServiceClient();
        setField(controller, "bannerServiceClient", client);

        BannerQo qo = new BannerQo();
        qo.setId(3L);
        qo.setTitle("banner");
        qo.setImgpath("/img/banner.png");

        assertSuccessWithoutPayload(controller.save(qo));
        assertEquals(3L, client.saved.getId().longValue());

        assertSuccessWithoutPayload(controller.upDown(3L, "1"));
        assertEquals(3L, client.upDown.getId().longValue());
        assertEquals("1", client.upDown.getStatus());
    }

    @Test
    public void articleSaveAndUpDownTreatVoidServiceResponseAsSuccess() throws Exception {
        AdminArticleController controller = new AdminArticleController();
        RecordingArticleServiceClient client = new RecordingArticleServiceClient();
        setField(controller, "articleServiceClient", client);

        ArticleQo qo = new ArticleQo();
        qo.setId(4L);
        qo.setTitle("article");

        assertSuccessWithoutPayload(controller.save(qo));
        assertEquals(4L, client.saved.getId().longValue());

        assertSuccessWithoutPayload(controller.upDown(4L, "1"));
        assertEquals(4L, client.upDown.getId().longValue());
        assertEquals("1", client.upDown.getStatus());
    }

    @Test
    public void productSaveAndUpDownTreatVoidServiceResponseAsSuccess() throws Exception {
        AdminProductController controller = new AdminProductController();
        RecordingProductServiceClient client = new RecordingProductServiceClient();
        setField(controller, "productServiceClient", client);

        ProductQo qo = new ProductQo();
        qo.setId(5L);
        qo.setName("product");

        assertSuccessWithoutPayload(controller.save(qo));
        assertEquals(5L, client.saved.getId().longValue());

        assertSuccessWithoutPayload(controller.upDown(5L, "0"));
        assertEquals(5L, client.upDown.getId().longValue());
        assertEquals("0", client.upDown.getStatus());
    }

    private void assertSuccessWithoutPayload(ResponseApiBody response) {
        assertEquals(ResponseCode.SUCCESS.getCode(), response.getCode());
        assertEquals(ResponseCode.SUCCESS.getMsg(), response.getMsg());
        assertNull(response.getRes());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class RecordingBannerServiceClient implements BannerServiceClient {
        private BannerQo saved;
        private BannerQo upDown;

        @Override
        public String get(Long id) {
            return "{}";
        }

        @Override
        public String findPage(BannerQo bannerQo) {
            return "{}";
        }

        @Override
        public void add(BannerQo bannerQo) {
            this.saved = bannerQo;
        }

        @Override
        public void upDown(BannerQo bannerQo) {
            this.upDown = bannerQo;
        }
    }

    private static class RecordingArticleServiceClient implements ArticleServiceClient {
        private ArticleQo saved;
        private ArticleQo upDown;

        @Override
        public String get(Long id) {
            return "{}";
        }

        @Override
        public String findPage(ArticleQo articleQo) {
            return "{}";
        }

        @Override
        public void add(ArticleQo articleQo) {
            this.saved = articleQo;
        }

        @Override
        public void upDown(ArticleQo articleQo) {
            this.upDown = articleQo;
        }
    }

    private static class RecordingProductServiceClient implements ProductServiceClient {
        private ProductQo saved;
        private ProductQo upDown;

        @Override
        public String get(Long id) {
            return "{}";
        }

        @Override
        public String findPage(ProductQo productQo) {
            return "{}";
        }

        @Override
        public void add(ProductQo productQo) {
            this.saved = productQo;
        }

        @Override
        public void upDown(ProductQo productQo) {
            this.upDown = productQo;
        }
    }
}
