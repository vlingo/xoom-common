package io.vlingo.common.config;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvVarProperties extends Properties {
    private static final Pattern envVarPattern = Pattern.compile("\\$\\{(?<envVar>[0-9a-zA-Z_]+)(:(?<default>[^\\}]+))?\\}");

    /**
     * Checks if the value contains a sequence to be interpolated (${&lt;ENV_VAR&gt;}) and
     * replaces it with the value of the corresponding environment variable.
     * <p>
     * Default values can be specified after a colon in the variable: <code>${ENV_VAR:default}</code>
     * <p>
     * The implementation overrides <code>put</code> of the underlying <code>HashMap</code>
     * to hook into the process of setting the value.
     *
     * @throws IllegalArgumentException if the environment variable is not set or either key or value are null
     * @see Properties#setProperty
     * @see Properties#load
     */
    @Override
    public synchronized Object put(Object key, Object value) {
        ensureKeyAndValueAreStrings(key, value);

        StringBuffer interpolated = new StringBuffer();
        Matcher matcher = envVarPattern.matcher((String) value);

        while (matcher.find()) {
            String envVal = System.getenv(matcher.group("envVar"));
            String defaultVal = matcher.group("default");

            ensureValueOrDefault((String) key, envVal, defaultVal);
            matcher.appendReplacement(interpolated, envVal == null ? defaultVal : envVal);
        }
        matcher.appendTail(interpolated);

        return super.put(key, interpolated.toString());
    }

    private void ensureKeyAndValueAreStrings(Object key, Object value) {
        if (!(key instanceof String && value instanceof String)) {
            throw new IllegalArgumentException("Both key and value need to be Strings");
        }
    }

    private void ensureValueOrDefault(String key, String envVal, String defaultVal) {
        if (envVal == null && defaultVal == null) {
            throw new IllegalArgumentException("Environment variable " + key + " not set and has no default.");
        }
    }
}
