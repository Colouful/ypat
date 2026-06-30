package com.ypat.repository;

import com.ypat.entity.InviteRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRelationRepository
        extends JpaRepository<InviteRelation, Long>, JpaSpecificationExecutor<InviteRelation> {

    InviteRelation findByInviteeUserid(Long inviteeUserid);

    long countByInviterUserid(Long inviterUserid);
}
