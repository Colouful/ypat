package com.ypat.service;

import com.ypat.entity.InviteRelation;
import com.ypat.repository.InviteRelationRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InviteServiceRewardTest {
    private InviteService inviteService;
    private List<InviteRelation> relations;

    @Before
    public void setUp() {
        inviteService = new InviteService();
        relations = new ArrayList<InviteRelation>();
        ReflectionTestUtils.setField(inviteService, "inviteRelationRepository", relationRepository(relations));
    }

    @Test
    public void bindRelationOnlyCreatesFirstRelationForInvitee() {
        InviteService.BindRelationResult first =
                inviteService.bindRelationIfAbsent(1L, 2L, "IV1", "share", 8);
        InviteService.BindRelationResult second =
                inviteService.bindRelationIfAbsent(1L, 2L, "IV1", "share", 8);

        assertTrue(first.isCreated());
        assertFalse(second.isCreated());
        assertEquals(1, relations.size());
        assertEquals(Integer.valueOf(8), relations.get(0).getRewardPpd());
    }

    @Test
    public void bindRelationRejectsSelfInvite() {
        InviteService.BindRelationResult result =
                inviteService.bindRelationIfAbsent(2L, 2L, "IV2", "share", 3);

        assertFalse(result.isCreated());
        assertEquals(0, relations.size());
    }

    private static InviteRelationRepository relationRepository(List<InviteRelation> relations) {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("toString".equals(method.getName())) return "InviteRelationRepositoryProxy";
                if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                if ("equals".equals(method.getName())) return proxy == args[0];
                if ("findByInviteeUserid".equals(method.getName())) {
                    Long inviteeUserid = (Long) args[0];
                    for (InviteRelation item : relations) {
                        if (inviteeUserid.equals(item.getInviteeUserid())) return item;
                    }
                    return null;
                }
                if ("save".equals(method.getName())) {
                    InviteRelation relation = (InviteRelation) args[0];
                    relations.add(relation);
                    return relation;
                }
                if ("countByInviterUserid".equals(method.getName())) return 0L;
                throw new UnsupportedOperationException(method.getName());
            }
        };
        return (InviteRelationRepository) Proxy.newProxyInstance(
                InviteRelationRepository.class.getClassLoader(),
                new Class[]{InviteRelationRepository.class},
                handler
        );
    }
}
