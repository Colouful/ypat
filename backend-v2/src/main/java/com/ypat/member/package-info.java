/**
 * member sub-domain — folded into {@code com.ypat.user} per V1.1 §2.2.
 *
 * Why this package still exists even though V1.1 says member
 * becomes part of user:
 *
 *   - The legacy code keeps "user" and "member" as separate
 *     services with separate tables (t_user_member, t_member_plan,
 *     t_member_order). Folding them at the code level without
 *     folding them at the table level would create a split-personality
 *     user entity.
 *   - V1.1 §2.2 says the merge happens at the **module** level:
 *     one module owns both surfaces. So we keep the sub-package
 *     around for the code that genuinely belongs to "member tier
 *     / plan / expiry" — it just publishes its public API through
 *     com.ypat.user.application now, not com.ypat.member.application.
 *   - When the database schema is also merged (a later Flyway
 *     migration), the com.ypat.member package will be deleted
 *     entirely. Until then it's a sub-domain.
 *
 * Public API surface for callers: com.ypat.user.application
 * (the MembershipUseCase is the front door).
 *
 * @NamedInterface marks com.ypat.member.api as published, so
 * legacy code referring to it (during the cut-over) keeps
 * resolving through Modulith's verify().
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.member;