package com.bdbank.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A single, reusable Jackson ObjectMapper for the whole app. ObjectMapper is expensive to create
 * and is thread-safe for read/write operations once configured, so - same idea as
 * ExecutorServiceManager's shared thread pool - we build it once here instead of calling
 * `new ObjectMapper()` everywhere JSON is touched.
 */
public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {}
}
