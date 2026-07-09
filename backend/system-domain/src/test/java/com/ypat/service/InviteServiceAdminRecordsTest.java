package com.ypat.service;

import com.ypat.InviteRelationQo;
import com.ypat.entity.InviteRelation;
import com.ypat.repository.InviteRelationRepository;
import com.ypat.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class InviteServiceAdminRecordsTest {
    private InviteService inviteService;
    private List<InviteRelation> relations;

    @Before
    public void setUp() {
        inviteService = new InviteService();
        relations = new ArrayList<InviteRelation>();
        InviteRelation relation = new InviteRelation();
        relation.setId(1L);
        relation.setInviterUserid(10L);
        relation.setInviteeUserid(20L);
        relation.setInviteCode("ABC123");
        relation.setSource("share");
        relation.setRewardPpd(3);
        relations.add(relation);
        ReflectionTestUtils.setField(inviteService, "inviteRelationRepository", relationRepository(relations));
        ReflectionTestUtils.setField(inviteService, "userRepository", userRepository());
    }

    @Test
    public void adminFindPageDoesNotRequireInviterFilter() {
        InviteRelationQo qo = new InviteRelationQo();

        Map<String, Object> page = inviteService.adminFindPage(qo);

        assertEquals(1L, page.get("totalElements"));
        assertEquals(1, ((List<?>) page.get("content")).size());
    }

    private static InviteRelationRepository relationRepository(List<InviteRelation> relations) {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("toString".equals(method.getName())) return "InviteRelationRepositoryProxy";
                if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                if ("equals".equals(method.getName())) return proxy == args[0];
                if ("findAll".equals(method.getName()) && args != null && args.length == 2) {
                    return new PageImpl<InviteRelation>(relations, (Pageable) args[1], relations.size());
                }
                throw new UnsupportedOperationException(method.getName());
            }
        };
        return (InviteRelationRepository) Proxy.newProxyInstance(
                InviteRelationRepository.class.getClassLoader(),
                new Class[]{InviteRelationRepository.class},
                handler
        );
    }

    private static UserRepository userRepository() {
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("toString".equals(method.getName())) return "UserRepositoryProxy";
                if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                if ("equals".equals(method.getName())) return proxy == args[0];
                if ("findOne".equals(method.getName())) return null;
                throw new UnsupportedOperationException(method.getName());
            }
        };
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                handler
        );
    }
}
