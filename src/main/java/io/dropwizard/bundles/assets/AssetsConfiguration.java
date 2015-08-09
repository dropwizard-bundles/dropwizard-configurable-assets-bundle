package io.dropwizard.bundles.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import java.util.Map;
import javax.validation.constraints.NotNull;

public class AssetsConfiguration {
  public static final String SLASH = "/";
  @JsonProperty
  private Map<String, String> mappings = Maps.newHashMap();

  protected Map<String, String> mappings() {
    return mappings;
  }

  /**
   * Initialize cacheSpec to null so that whatever may be specified by code is able to be
   * by configuration. If null the default cache spec of "maximumSize=100" will be used.
   *
   * @see ConfiguredAssetsBundle#DEFAULT_CACHE_SPEC
   */

  @JsonProperty
  private String cacheSpec = null;

  @NotNull
  @JsonProperty
  private Map<String, String> overrides = Maps.newHashMap();

  @NotNull
  @JsonProperty
  private Map<String, String> mimeTypes = Maps.newHashMap();

  private Map<String, String> resourcePathToUriMappings;
  /**
   * A series of mappings from resource paths (in the classpath)
   * to the uri path that hosts the resource
   * @return The resourcePathToUriMappings.
   */
  public Map<String, String> getResourcePathToUriMappings() {
    if (resourcePathToUriMappings == null) {
      ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.<String, String>builder();
      // Ensure that resourcePath and uri ends with a '/'
      for (Map.Entry<String, String> mapping : mappings().entrySet()) {
        mapBuilder
            .put(ensureEndsWithSlash(mapping.getKey()), ensureEndsWithSlash(mapping.getValue()));
      }
      resourcePathToUriMappings = mapBuilder.build();
    }
    return resourcePathToUriMappings;
  }

  private String ensureEndsWithSlash(String value) {
    return value != null ? (value.endsWith(SLASH) ? value : value + SLASH) : SLASH;
  }

  /**
   * The caching specification for how to memoize assets.
   *
   * @return The cacheSpec.
   */
  public String getCacheSpec() {
    return cacheSpec;
  }

  public Iterable<Map.Entry<String, String>> getOverrides() {
    return Iterables.unmodifiableIterable(overrides.entrySet());
  }

  public Iterable<Map.Entry<String, String>> getMimeTypes() {
    return Iterables.unmodifiableIterable(mimeTypes.entrySet());
  }
}
