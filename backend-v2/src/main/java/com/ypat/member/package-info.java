/**
 * member module — member plan, benefits, expiry.
 *
 * Owner: v2, write: true, read: true, migrated_at: TBD
 *
 * Public API:
 *   - com.ypat.member.api
 *   - com.ypat.member.application
 *
 * Notes:
 *   - V1.1 §2.2 merges member into user/. Until PR-17 the two
 *     packages stay separate so legacy code can keep its
 *     current com.ypat.service.MemberService imports while
 *     v2 builds up the user side.
 */
@org.springframework.lang.NonNullApi
package com.ypat.member;