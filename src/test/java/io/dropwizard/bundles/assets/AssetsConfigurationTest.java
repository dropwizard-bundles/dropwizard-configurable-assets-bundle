package io.dropwizard.bundles.assets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;


import java.util.HashMap;
import java.util.Map;

public class AssetsConfigurationTest {
  private AssetsConfiguration config;

  private final static String A_RESOURCE_PATH = "/a/resource/path";
  private final static String AN_URI = "/an/uri";

  @Before
  public void setupConfig() throws Exception {
    final Map<String, String> mappings = new HashMap<>();
    mappings.put(A_RESOURCE_PATH, AN_URI);
    config = new AssetsConfiguration() {
      @Override
      protected Map<String, String> mappings() {
        return mappings;
      }
    };
  }

  @After
  public void clearConfig() throws Exception {
    config = null;
  }

  @Test
  public void keysAndValuesInTheResourcePathUriMappinsAlwaysEndWithSlash() {
    Map<String, String> actualMappings = config.getResourcePathToUriMappings();
    Assert.assertNotNull(actualMappings);
    Assert.assertEquals(1, actualMappings.size());
    Assert.assertTrue(actualMappings.containsKey(A_RESOURCE_PATH + "/"));
    Assert.assertEquals(AN_URI + "/", actualMappings.get(A_RESOURCE_PATH + "/"));
  }
}
