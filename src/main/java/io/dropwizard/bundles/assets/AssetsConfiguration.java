package io.dropwizard.bundles.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import javax.validation.constraints.NotNull;

public class AssetsConfiguration {
  public static final String SLASH = "/";

  @JsonProperty
  private Map<String, String> mappings = Maps.newHashMap();

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

  @JsonProperty
  private String cacheControlHeader = null;

  private Map<String, String> resourcePathToUriMappings;

  private AssetsConfiguration(
      String cacheSpec,
      Map<String, String> mappings,
      Map<String, String> mimeTypes,
      Map<String, String> overrides) {
    this.cacheSpec = cacheSpec;
    this.mappings = Collections.unmodifiableMap(mappings);
    this.mimeTypes = Collections.unmodifiableMap(mimeTypes);
    this.overrides = Collections.unmodifiableMap(overrides);
  }

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

  protected Map<String, String> mappings() {
    return mappings;
  }

  /**
   * The caching specification for how to memoize assets.
   *
   * @return The cacheSpec.
   */
  public String getCacheSpec() {
    return cacheSpec;
  }

  public Map<String, String> getOverrides() {
    return Collections.unmodifiableMap(overrides);
  }

  public Map<String, String> getMimeTypes() {
    return Collections.unmodifiableMap(mimeTypes);
  }

  public String getCacheControlHeader() {
    return cacheControlHeader;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String cacheSpec;
    private Map<String, String> mappings = Maps.newHashMap();
    private Map<String, String> mimeTypes = Maps.newHashMap();
    private Map<String, String> overrides = Maps.newHashMap();

    private Builder() {}

    public Builder cacheSpec(String cacheSpec) {
      this.cacheSpec = cacheSpec;
      return this;
    }

    public Builder mappings(Map<String, String> mappings) {
      this.mappings = Preconditions.checkNotNull(mappings);
      return this;
    }

    public Builder mimeTypes(Map<String, String> mimeTypes) {
      this.mimeTypes = Preconditions.checkNotNull(mimeTypes);
      return this;
    }

    public Builder overrides(Map<String, String> overrides) {
      this.overrides = Preconditions.checkNotNull(overrides);
      return this;
    }

    public AssetsConfiguration build() {
      return new AssetsConfiguration(cacheSpec, mappings, mimeTypes, overrides);
    }
  }

}
