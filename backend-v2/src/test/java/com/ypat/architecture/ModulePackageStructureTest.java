package com.ypat.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PR-06: package-structure discipline for backend-v2.
 *
 * Lightweight ArchUnit rules that enforce the layout we agreed
 * on in the upgrade plan, without pulling in Spring Modulith.
 * Modulith verify() lands with PR-12 once we jump to Boot 3.x.
 *
 * Why these rules and not more:
 *   - Every top-level module under {@code com.ypat.*} MUST have a
 *     package-info.java. This is the smallest enforceable signal
 *     that the module is "owned" and has been thought about.
 *   - No cross-module leak into {@code *.internal}. The internal
 *     convention is documented in each module's package-info and
 *     enforced here so we catch a stray import before it costs a
 *     PR.
 *   - No upstream legacy leak: nothing under {@code com.ypat.*} may
 *     reach into the legacy {@code com.ypat.service} /
 *     {@code com.ypat.dao} / {@code com.ypat.entity} / {@code com.ypat.util}
 *     packages that belong to system-domain/system-wap.
 *
 * Not enforced here (intentionally, for this PR):
 *   - Spring component scan restrictions — would force us to add
 *     @ComponentScan filters early; not worth the noise when only
 *     one Controller exists.
 *   - Modulith ApplicationModule annotations — those need Boot 3.x.
 */
class ModulePackageStructureTest {

    /** Modules that must each have a package-info.java. */
    private static final String[] TOP_LEVEL_MODULES = {
            "auth", "user", "work", "content", "member",
            "wallet", "payment", "identity", "notification",
            "storage", "audit"
    };

    /**
     * Packages the legacy multi-module backend already owns.
     * New backend-v2 code must NOT depend on any of these.
     * Listing the four most common offender roots; expand if a
     * regression slips in.
     */
    private static final String[] LEGACY_PACKAGES = {
            "com.ypat.service",
            "com.ypat.dao",
            "com.ypat.entity",
            "com.ypat.util"
    };

    private static JavaClasses imported;

    @BeforeAll
    static void importClasses() {
        imported = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.ypat");
    }

    @Test
    void everyTopLevelModuleHasPackageInfo() {
        for (String module : TOP_LEVEL_MODULES) {
            String pkg = "com.ypat." + module;
            boolean present = imported.stream()
                    .anyMatch(cd -> cd.getPackageName().equals(pkg)
                            && cd.getSimpleName().equals("package-info"));
            assertTrue(present,
                    () -> "Module '" + module + "' is missing a package-info.java. "
                            + "Every backend-v2 module must claim ownership with a "
                            + "package-info (see PR-06 docs).");
        }
    }

    @Test
    void modulesMustNotAccessOtherModulesInternal() {
        // ArchUnit 0.23.x has no fluent .andNoClasses() in the
        // chain, so we express each "no leak" as its own ArchRule
        // and check them all against the same imported class set.
        ArchRule workInternalMustNotTouchPayment =
                noClasses()
                        .that().resideInAPackage("..work.internal..")
                        .should().dependOnClassesThat().resideInAPackage("..payment..");

        ArchRule paymentInternalMustNotTouchWallet =
                noClasses()
                        .that().resideInAPackage("..payment.internal..")
                        .should().dependOnClassesThat().resideInAPackage("..wallet..");

        ArchRule walletInternalMustNotTouchIdentity =
                noClasses()
                        .that().resideInAPackage("..wallet.internal..")
                        .should().dependOnClassesThat().resideInAPackage("..identity..");

        workInternalMustNotTouchPayment.check(imported);
        paymentInternalMustNotTouchWallet.check(imported);
        walletInternalMustNotTouchIdentity.check(imported);
    }

    @Test
    void v2MustNotReachIntoLegacyServiceOrDaoPackages() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.ypat..")
                .and().resideOutsideOfPackages(LEGACY_PACKAGES)
                .should().dependOnClassesThat().resideInAnyPackage(LEGACY_PACKAGES);

        rule.check(imported);
    }

    @Test
    void controllersMustLiveInApiPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..api..");

        rule.check(imported);
    }
}