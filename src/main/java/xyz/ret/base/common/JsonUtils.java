package xyz.ret.base.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ObjectMapper OBJECT_MAPPER_DEFAULT_TYPE = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        OBJECT_MAPPER.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, Boolean.FALSE);
    }

    static {
        OBJECT_MAPPER_DEFAULT_TYPE.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER_DEFAULT_TYPE.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public static String toJsonWithDefaultType(Object o) {
        try {
            return OBJECT_MAPPER_DEFAULT_TYPE.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Json serialization error", e);
            return "";
        }
    }

    public static Object toBeanWithDefaultType(String json) {
        try {
            return OBJECT_MAPPER_DEFAULT_TYPE.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Json deserialization {}", json, e);
            return null;
        }
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Json deserialization {}", json, e);
            return null;
        }
    }

    public static <T> T toBeans(String json, Class collectionClass, Class<T> elementClasses) {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            log.error("Json deserialization {}", json, e);
            return null;
        }
    }

    public static <T> T toBean(String json, TypeReference<T> typeRef) {
        try {
            return (T) OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Json deserialization {}", json, e);
            return null;
        }
    }

    public static <T> String toJson(T t) {
        try {
            return OBJECT_MAPPER.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String toJson(T t, Class jsonView) {
        try {
            return OBJECT_MAPPER.writerWithView(jsonView).writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}