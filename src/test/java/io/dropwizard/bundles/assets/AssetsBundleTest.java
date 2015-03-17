package io.dropwizard.bundles.assets;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.servlets.assets.ResourceURL;
import io.dropwizard.setup.Environment;
import java.net.URL;
import java.util.List;
import javax.servlet.ServletRegistration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssetsBundleTest {
  private final ServletEnvironment servletEnvironment = mock(ServletEnvironment.class);
  private final Environment environment = mock(Environment.class);

  private String servletPath;
  private List<String> servletPaths;

  @Before
  public void setUp() throws Exception {
    when(environment.servlets()).thenReturn(servletEnvironment);
  }

  @Test
  public void hasADefaultPath() throws Exception {
    runBundle(new ConfiguredAssetsBundle());

    assertThat(servletPath)
            .isEqualTo("/assets/*");
  }

  @Test
  public void canHaveCustomPaths() throws Exception {
    runBundle(new ConfiguredAssetsBundle("/json"));

    assertThat(servletPath)
            .isEqualTo("/json/*");
  }

  @Test
  public void canHaveDifferentUriAndResourcePaths() throws Exception {
    runBundle(new ConfiguredAssetsBundle("/json", "/what"));

    assertThat(servletPath)
            .isEqualTo("/what/*");
  }

  @Test
  public void canSupportDifferentAssetsBundleName() throws Exception {
    runBundle(new ConfiguredAssetsBundle("/json", "/what/new", "index.txt", "customAsset1"),
            "customAsset1");

    assertThat(servletPath)
            .isEqualTo("/what/new/*");

    runBundle(new ConfiguredAssetsBundle("/json", "/what/old", "index.txt", "customAsset2"),
            "customAsset2");
    assertThat(servletPath)
            .isEqualTo("/what/old/*");
  }

  @Test
  public void canHaveDifferentUriAndResourcePathsAndIndexFilename() throws Exception {
    runBundle(new ConfiguredAssetsBundle("/json", "/what", "index.txt"));

    assertThat(servletPath)
            .isEqualTo("/what/*");
  }

  @Test
  public void canHaveMultipleMappings() throws Exception {
    runBundle(new ConfiguredAssetsBundle(ImmutableMap.<String, String>builder()
            .put("/risk", "/riskPath")
            .put("/catan", "/catanPath")
            .build()
    ));

    assertThat(servletPaths.size()).isEqualTo(2);
    assertThat(servletPaths).contains("/riskPath/*");
    assertThat(servletPaths).contains("/catanPath/*");
  }

  private void runBundle(ConfiguredAssetsBundle bundle) throws Exception {
    runBundle(bundle, "assets");
  }

  private void runBundle(ConfiguredAssetsBundle bundle, String assetName) throws Exception {
    final ServletRegistration.Dynamic registration = mock(ServletRegistration.Dynamic.class);
    when(servletEnvironment.addServlet(anyString(), any(AssetServlet.class)))
            .thenReturn(registration);
    AssetsBundleConfiguration defaultConfiguration = new AssetsBundleConfiguration() {
      @Override
      public AssetsConfiguration getAssetsConfiguration() {
        return new AssetsConfiguration();
      }
    };

    bundle.run(defaultConfiguration, environment);

    final ArgumentCaptor<AssetServlet> servletCaptor = ArgumentCaptor.forClass(AssetServlet.class);
    final ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);

    verify(servletEnvironment, atLeastOnce()).addServlet(eq(assetName), servletCaptor.capture());
    verify(registration, atLeastOnce()).addMapping(pathCaptor.capture());

    this.servletPath = pathCaptor.getValue();
    this.servletPaths = pathCaptor.getAllValues();

    // If more than one servlet was captured, let's verify they're the same instance.
    List<AssetServlet> capturedServlets = servletCaptor.getAllValues();
    if (capturedServlets.size() > 1) {
      for (AssetServlet servlet : capturedServlets) {
        assertThat(servlet == capturedServlets.get(0));
      }
    }
  }
}
