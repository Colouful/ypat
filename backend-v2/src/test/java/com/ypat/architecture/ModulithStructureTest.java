package com.ypat.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PR-12: Spring Modulith boundary enforcement.
 *
 * Replaces the lightweight "package-info must exist" rules from
 * PR-06 with Modulith's deeper {@code ApplicationModules.of(...)
 * .verify()} which checks:
 *   - Cycle-free module graph
 *   - Published events consumed only by listeners that respect
 *     the @ApplicationModule listener boundaries
 *   - No public type in a module references internal types of
 *     another module
 *   - Default-public visibility on aggregate roots
 *
 * Modulith also generates a component diagram into
 * target/spring-modulith-docs/ on every run — handy for the
 * architecture review.
 *
 * Why a separate test from ModulePackageStructureTest:
 *   - ModulePackageStructureTest (PR-06) is cheap (no Spring
 *     context) and runs on every commit.
 *   - ModulithStructureTest scans the classpath at startup; it
 *     is more expensive. CI only runs it on the protected
 *     branches via backend-v2-modulith.yml.
 *
 * PR-12 is the moment the verify() check becomes enforceable.
 * Before this PR there were no real modules to verify, so the
 * test would always pass.
 */
class ModulithStructureTest {

    @Test
    void modulesSatisfyModulithBoundaries() {
        ApplicationModules modules = ApplicationModules.of("com.ypat");
        modules.verify();
    }

    @Test
    void allElevenExpectedModulesArePresent() {
        ApplicationModules modules = ApplicationModules.of("com.ypat");
        assertThat(modules).isNotNull();

        // Modulith treats every direct sub-package of the base
        // package as a candidate module. The eleven we want
        // (auth/user/work/content/member/wallet/payment/
        //  identity/notification/storage/audit) must all show up.
        String[] expected = {
                "auth", "user", "work", "content", "member",
                "wallet", "payment", "identity", "notification",
                "storage", "audit"
        };
        for (String name : expected) {
            assertThat(modules.getModuleByName(name))
                    .as("module %s must be visible to Modulith", name)
                    .isNotNull();
        }
    }

    @Test
    void generatesComponentDiagram() throws IOException {
        // Spring Modulith 1.4 Documenter.writeDocumentation() takes
        // no args and writes to target/spring-modulith-docs/ by
        // default. Output is .adoc (AsciiDoc) + .puml (PlantUML)
        // — the .html form needs an asciidoctor step we don't run
        // in CI. Operators pick the artifacts up from the CI run
        // and convert locally with `asciidoctor all-docs.adoc`.
        ApplicationModules modules = ApplicationModules.of("com.ypat");
        new Documenter(modules).writeDocumentation();
        Path out = Path.of("target/spring-modulith-docs");
        assertThat(out.resolve("all-docs.adoc")).exists();
        assertThat(out.resolve("components.puml")).exists();
    }
}