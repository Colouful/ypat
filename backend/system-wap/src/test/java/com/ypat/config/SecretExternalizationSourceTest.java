package com.ypat.config;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class SecretExternalizationSourceTest {

    @Test
    public void repositoryDoesNotContainKnownHistoricalSecrets() throws IOException {
        String source = readRepositoryText();
        List<String> forbidden = Arrays.asList(
                "WNFSIDH" + "FIOWEF#$%&*9984334" + "Secret" + "SOFOJWNFOWIJFSLSIJDF",
                "Li" + "123456.",
                "DY" + "X~!@#$" + "123qwe",
                "tc" + "123456",
                "sso" + "secret0",
                "sso" + "secret1",
                "FastDFS" + "1234567890"
        );

        for (String value : forbidden) {
            assertFalse("历史密钥不得出现在源码或配置中: " + value, source.contains(value));
        }
    }

    @Test
    public void ssoModuleDoesNotShipKeystoreFiles() {
        assertFalse(Files.exists(resolve("backend/system-sso/src/main/resources/dev/keystore.jks")));
        assertFalse(Files.exists(resolve("backend/system-sso/src/main/resources/pro/keystore.jks")));
    }

    private String readRepositoryText() throws IOException {
        StringBuilder builder = new StringBuilder();
        List<String> files = Arrays.asList(
                "docker-compose.yml",
                "backend/system-wap/src/main/java/com/ypat/comm/Const.java",
                "backend/system-web/src/main/java/com/ypat/comm/Const.java",
                "backend/system-sso/src/main/java/com/ypat/config/OAuthConfigurer.java",
                "backend/system-sso/src/main/resources/dev/application.yml",
                "backend/system-sso/src/main/resources/pro/application.yml",
                "backend/system-restapi/src/main/resources/dev/application.yml",
                "backend/system-restapi/src/main/resources/pro/application.yml",
                "backend/system-wap/src/main/resources/conf/fdfs_client.properties",
                "backend/system-web/src/main/resources/conf/fdfs_client.properties",
                "backend-base/base-zipkin/src/main/resources/bootstrap.yml",
                "backend-base/base-turbine/src/main/resources/bootstrap.yml",
                "backend-base/base-config/src/main/resources/bootstrap.yml"
        );
        for (String file : files) {
            Path path = resolve(file);
            if (Files.exists(path)) {
                builder.append(new String(Files.readAllBytes(path), StandardCharsets.UTF_8)).append('\n');
            }
        }
        return builder.toString();
    }

    private Path resolve(String file) {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path direct = current.resolve(file);
            if (Files.exists(direct)) {
                return direct;
            }
            Path fromBackend = current.resolve("backend").resolve(file.replaceFirst("^backend/", ""));
            if (Files.exists(fromBackend)) {
                return fromBackend;
            }
            current = current.getParent();
        }
        return Paths.get(file);
    }
}
