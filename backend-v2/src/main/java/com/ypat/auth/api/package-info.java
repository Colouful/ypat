/**
 * auth.api sub-package: published surface of the auth module.
 *
 * The {@code @NamedInterface} annotation on this package-info
 * declares the whole {@code com.ypat.auth.api} sub-tree as the
 * module's public API. Other modules (user, work, ...) can depend
 * on types in here without Modulith's verify() complaining.
 *
 * Anything NOT in this sub-tree (e.g. {@code com.ypat.auth.internal},
 * {@code com.ypat.auth.application}) stays internal.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.lang.NonNullApi
package com.ypat.auth.api;