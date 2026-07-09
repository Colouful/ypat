package com.ypat.service;

import com.ypat.ResponseCode;
import com.ypat.SysException;
import com.ypat.YpatInfoQo;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class YpatInfoAdminFilterSourceTest {
    private static String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    @Test
    public void ypatAdminFiltersIncludeNewPublishFields() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(source.contains("queryQo.getTarget()"));
        assertTrue(source.contains("root.get(\"target\")"));
        assertTrue(source.contains("queryQo.getPatstyle()"));
        assertTrue(source.contains("root.get(\"patstyle\")"));
        assertTrue(source.contains("queryQo.getChargeway()"));
        assertTrue(source.contains("root.get(\"chargeway\")"));
        assertTrue(source.contains("queryQo.getWorkId()"));
        assertTrue(source.contains("root.get(\"workId\")"));
    }

    @Test
    public void ypatAdminWorkIdFilterRejectsInvalidInputWithBusinessError() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(source.contains("info.setWorkId(parseWorkIdValue(ypatInfo.getWorkId()))"));
        assertTrue(source.contains("catch (NumberFormatException"));
        assertTrue(source.contains("workId <= 0"));
        assertTrue(source.contains("new SysException(ResponseCode.FAIL_PARA"));
        assertTrue(source.contains("workId参数错误"));
    }

    @Test
    public void workIdFilterValidationRejectsInvalidValues() {
        YpatInfoService service = new YpatInfoService();
        YpatInfoQo emptyQo = new YpatInfoQo();
        assertNull(service.parseWorkIdFilter(emptyQo));

        YpatInfoQo validQo = new YpatInfoQo();
        validQo.setWorkId("12");
        assertEquals(Long.valueOf(12L), service.parseWorkIdFilter(validQo));
        assertEquals(Long.valueOf(12L), service.parseWorkIdValue("12"));

        assertWorkIdFailPara(service, "abc");
        assertWorkIdFailPara(service, "0");
        assertWorkIdFailPara(service, "-1");
    }

    @Test
    public void ypatAdminPatstyleFilterMatchesCommaSeparatedBoundaries() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertFalse(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), \"%\" + queryQo.getPatstyle() + \"%\")"));
        assertTrue(source.contains("normalizePatstyleFilters(queryQo.getPatstyle())"));
        assertTrue(source.contains("patstyleFilter.split(\",\")"));
        assertTrue(source.contains(".trim()"));
        assertTrue(source.contains("Set<String> patstyles"));
        assertTrue(source.contains("patstyles.add(patstyle)"));
        assertTrue(source.contains("YpatPatstyle.getNameByCode(patstyle)"));
        assertTrue(source.contains("new SysException(ResponseCode.FAIL_PARA, \"patstyle参数错误\")"));
        assertTrue(source.contains("criteriaBuilder.or("));
        assertTrue(source.contains("criteriaBuilder.equal(root.get(\"patstyle\"), patstyle)"));
        assertTrue(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), patstyle + \",%\")"));
        assertTrue(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), \"%,\" + patstyle)"));
        assertTrue(source.contains("criteriaBuilder.like(root.get(\"patstyle\"), \"%,\" + patstyle + \",%\")"));
    }

    @Test
    public void patstyleFilterValidationRejectsWildcardsAndNormalizesTokens() {
        YpatInfoService service = new YpatInfoService();

        assertPatstyleFailPara(service, ", ,");
        assertPatstyleFailPara(service, "%");
        assertPatstyleFailPara(service, "_");
        assertPatstyleFailPara(service, "1_");
        assertPatstyleFailPara(service, "13");

        Set<String> patstyles = service.normalizePatstyleFilters("1, 2,1");
        assertEquals(new LinkedHashSet<String>(Arrays.asList("1", "2")), patstyles);
    }

    @Test
    public void ypatAdminAuditOnlyUpdatesReviewFields() throws Exception {
        String source = read("src/main/java/com/ypat/service/YpatInfoService.java");
        String auditMethod = methodBody(source,
                "public void audit(Long id, String flag, String recomflag, String reason)",
                "public void delete(Long id)");

        assertTrue(auditMethod.contains("info.setStatus(flag)"));
        assertTrue(auditMethod.contains("if(recomflag!=null)"));
        assertTrue(auditMethod.contains("info.setRecomflag(recomflag)"));
        assertTrue(auditMethod.contains("info.setReason(reason)"));
        assertFalse(auditMethod.contains("setTarget"));
        assertFalse(auditMethod.contains("setPatstyle"));
        assertFalse(auditMethod.contains("setChargeway"));
        assertFalse(auditMethod.contains("setWorkId"));
    }

    @Test
    public void ypatAuthorPayloadIncludesMemberState() throws Exception {
        String userQoSource = read("../system-object/src/main/java/com/ypat/UserQo.java");
        String serviceSource = read("src/main/java/com/ypat/service/YpatInfoService.java");

        assertTrue(userQoSource.contains("private Boolean memberActive"));
        assertTrue(userQoSource.contains("private String memberLevel"));
        assertTrue(userQoSource.contains("getMemberActive()"));
        assertTrue(userQoSource.contains("setMemberActive(Boolean memberActive)"));
        assertTrue(userQoSource.contains("getMemberLevel()"));
        assertTrue(userQoSource.contains("setMemberLevel(String memberLevel)"));

        assertTrue(serviceSource.contains("private UserMemberRepository userMemberRepository"));
        assertTrue(serviceSource.contains("enrichMemberState(userQo, user.getId())"));
        assertTrue(serviceSource.contains("boolean isActiveMember(UserMember member)"));
        assertTrue(serviceSource.contains("member.getExpireAt().after(new Date())"));
        assertTrue(serviceSource.contains("!\"NONE\".equals(member.getLevel())"));
        assertTrue(serviceSource.contains("userQo.setMemberActive(true)"));
        assertTrue(serviceSource.contains("userQo.setMemberLevel(member.getLevel())"));
        assertTrue(serviceSource.contains("userQo.setMemberActive(false)"));
    }

    private String methodBody(String source, String startToken, String endToken) {
        int start = source.indexOf(startToken);
        assertTrue(start >= 0);
        int end = source.indexOf(endToken, start);
        assertTrue(end > start);
        return source.substring(start, end);
    }

    private void assertWorkIdFailPara(YpatInfoService service, String workId) {
        YpatInfoQo qo = new YpatInfoQo();
        qo.setWorkId(workId);
        try {
            service.parseWorkIdFilter(qo);
        } catch (SysException e) {
            assertEquals(ResponseCode.FAIL_PARA.getCode(), e.getCode());
            assertEquals("workId参数错误", e.getMsg());
            return;
        }
        throw new AssertionError("Expected FAIL_PARA for workId=" + workId);
    }

    private void assertPatstyleFailPara(YpatInfoService service, String patstyle) {
        try {
            service.normalizePatstyleFilters(patstyle);
        } catch (SysException e) {
            assertEquals(ResponseCode.FAIL_PARA.getCode(), e.getCode());
            assertEquals("patstyle参数错误", e.getMsg());
            return;
        }
        throw new AssertionError("Expected FAIL_PARA for patstyle=" + patstyle);
    }
}
