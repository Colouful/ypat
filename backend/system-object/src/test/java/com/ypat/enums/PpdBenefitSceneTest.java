package com.ypat.enums;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PpdBenefitSceneTest {
    @Test
    public void mapsSupportedCodesToChineseNames() {
        assertEquals("发布约拍", PpdBenefitScene.fromCode("SUBMIT_YPAT").getLabel());
        assertEquals("发起约拍申请", PpdBenefitScene.fromCode("APPLY_YPAT").getLabel());
        assertEquals("查看联系方式", PpdBenefitScene.fromCode("VIEW_CONTACT").getLabel());
        assertNull(PpdBenefitScene.fromCode("UNKNOWN"));
    }
}
