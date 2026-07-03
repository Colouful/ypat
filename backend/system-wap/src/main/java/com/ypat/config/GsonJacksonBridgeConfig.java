package com.ypat.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * 让 Spring 默认的 Jackson 能直接序列化 Gson 的 {@link JsonElement}。
 *
 * <p>Admin* Controller 从下游 Feign 客户端拿到的是 JSON 字符串，再用
 * {@code JsonParser.parseString(json)} 转成 Gson 的 {@link JsonElement}
 * 放进 {@code ResponseApiBody.res}。Jackson 默认序列化 JsonElement 时会当
 * 成普通 POJO 反射方法，命中 {@code asDouble()} 等 getter 抛
 * "Could not write JSON: JsonObject"。
 *
 * <p>JsonElement 的 {@code toString()} 本身就是合法 JSON，直接以 raw value
 * 写出即可。
 */
@Configuration
public class GsonJacksonBridgeConfig {

    @Bean
    public SimpleModule gsonJsonElementModule() {
        SimpleModule module = new SimpleModule("GsonJsonElementModule");
        module.addSerializer(JsonElement.class, new JsonElementSerializer());
        return module;
    }

    private static final class JsonElementSerializer extends JsonSerializer<JsonElement> {
        @Override
        public void serialize(JsonElement value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null || value instanceof JsonNull) {
                gen.writeNull();
                return;
            }
            gen.writeRawValue(value.toString());
        }
    }
}
