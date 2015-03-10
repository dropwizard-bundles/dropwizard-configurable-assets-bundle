package io.dropwizard.bundles.assets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.validation.constraints.NotNull;

public class AssetsConfiguration {
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
