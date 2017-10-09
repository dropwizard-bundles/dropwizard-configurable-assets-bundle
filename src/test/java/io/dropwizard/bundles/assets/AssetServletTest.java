package io.dropwizard.bundles.assets;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetServletTest {
  private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;
  private static final CacheBuilderSpec DEFAULT_CACHE_SPEC =
          CacheBuilderSpec.parse("maximumSize=100");
  private static final Iterable<Map.Entry<String, String>> EMPTY_OVERRIDES =
          ImmutableMap.<String, String>builder().build().entrySet();
  private static final Iterable<Map.Entry<String, String>> EMPTY_MIMETYPES =
          ImmutableMap.<String, String>builder().build().entrySet();
  private static final String DUMMY_SERVLET = "/dummy_servlet/";
  private static final String NOINDEX_SERVLET = "/noindex_servlet/";
  private static final String NOCHARSET_SERVLET = "/nocharset_servlet/";
  private static final String MIME_SERVLET = "/mime_servlet/";
  private static final String MM_ASSET_SERVLET = "/mm_assets/";
  private static final String MM_JSON_SERVLET = "/mm_json/";
  private static final String ROOT_SERVLET = "/";
  private static final String CACHE_SERVLET = "/cached/";
  private static final String RESOURCE_PATH = "/assets";
  private static final String JSON_RESOURCE_PATH = "/json";

  // ServletTester expects to be able to instantiate the servlet with zero arguments
  private static Iterable<Map.Entry<String, String>> resourceMapping(String resourcePath,
                                                                     String uriPath) {
    return ImmutableMap.<String, String>builder()
            .put(resourcePath, uriPath)
            .build()
            .entrySet();
  }

  public static class DummyAssetServlet extends AssetServlet {
    private static final long serialVersionUID = -1L;

    public DummyAssetServlet() {
      super(resourceMapping(RESOURCE_PATH, DUMMY_SERVLET), "index.htm", DEFAULT_CHARSET,
              DEFAULT_CACHE_SPEC, EMPTY_OVERRIDES, EMPTY_MIMETYPES);
    }
  }

  public static class NoIndexAssetServlet extends AssetServlet {
    private static final long serialVersionUID = -1L;

    public NoIndexAssetServlet() {
      super(resourceMapping(RESOURCE_PATH, DUMMY_SERVLET), null, DEFAULT_CHARSET,
              DEFAULT_CACHE_SPEC, EMPTY_OVERRIDES, EMPTY_MIMETYPES);
    }
  }

  public static class RootAssetServlet extends AssetServlet {
    private static final long serialVersionUID = 1L;

    public RootAssetServlet() {
      super(resourceMapping("/", ROOT_SERVLET), null, DEFAULT_CHARSET, DEFAULT_CACHE_SPEC,
              EMPTY_OVERRIDES, EMPTY_MIMETYPES);
    }
  }

  public static class NoCharsetAssetServlet extends AssetServlet {
    private static final long serialVersionUID = 1L;

    /** Constructor. */
    public NoCharsetAssetServlet() {
      super(resourceMapping(RESOURCE_PATH, NOCHARSET_SERVLET), null, DEFAULT_CHARSET,
              DEFAULT_CACHE_SPEC, EMPTY_OVERRIDES, EMPTY_MIMETYPES);
      setDefaultCharset(null);
    }
  }

  public static class MimeMappingsServlet extends AssetServlet {
    private static final long serialVersionUID = 1L;

    /** Constructor. */
    public MimeMappingsServlet() {
      super(resourceMapping(RESOURCE_PATH, MIME_SERVLET), null, DEFAULT_CHARSET, DEFAULT_CACHE_SPEC,
              EMPTY_OVERRIDES, EMPTY_MIMETYPES);
      Map<String, String> mimeMappings = new HashMap<>();
      mimeMappings.put("bar", "application/bar");
      mimeMappings.put("txt", "application/foo");
      setMimeTypes(mimeMappings.entrySet());
    }
  }

  public static class MultipleMappingsServlet extends AssetServlet {
    private static final long serialVersionUID = 1L;

    /** Constructor. */
    public MultipleMappingsServlet() {
      super(ImmutableMap.<String, String>builder()
                      .put(RESOURCE_PATH, MM_ASSET_SERVLET)
                      .put(JSON_RESOURCE_PATH, MM_JSON_SERVLET)
                      .build().entrySet(),
              null, DEFAULT_CHARSET, DEFAULT_CACHE_SPEC, EMPTY_OVERRIDES, EMPTY_MIMETYPES
      );
    }
  }

  public static class CachingServlet extends AssetServlet {
    private static final long serialVersionUID = -1L;

    /** Constructor. */
    public CachingServlet() {
      super(resourceMapping(RESOURCE_PATH, CACHE_SERVLET),null, DEFAULT_CHARSET, DEFAULT_CACHE_SPEC,
              EMPTY_OVERRIDES, EMPTY_MIMETYPES);
      setCacheControlHeader("public");
    }
  }

  private final ServletTester servletTester = new ServletTester();
  private final HttpTester.Request request = HttpTester.newRequest();
  private HttpTester.Response response;

  private HttpTester.Response makeRequest(String uri) throws Exception {
    request.setURI(uri);
    return makeRequest();
  }

  private HttpTester.Response makeRequest() throws Exception {
    return HttpTester.parseResponse(servletTester.getResponses(request.generate()));
  }

  @Before
  public void setup() throws Exception {
    servletTester.addServlet(DummyAssetServlet.class, DUMMY_SERVLET + '*');
    servletTester.addServlet(NoIndexAssetServlet.class, NOINDEX_SERVLET + '*');
    servletTester.addServlet(NoCharsetAssetServlet.class, NOCHARSET_SERVLET + '*');
    servletTester.addServlet(RootAssetServlet.class, ROOT_SERVLET + '*');
    servletTester.addServlet(MimeMappingsServlet.class, MIME_SERVLET + '*');
    servletTester.addServlet(CachingServlet.class, CACHE_SERVLET + '*');

    ServletHolder servlet = new ServletHolder(MultipleMappingsServlet.class);
    servletTester.addServlet(servlet, MM_ASSET_SERVLET + '*');
    servletTester.addServlet(servlet, MM_JSON_SERVLET + '*');
    servletTester.start();

    servletTester.getContext().getMimeTypes().addMimeMapping("mp4", "video/mp4");
    servletTester.getContext().getMimeTypes().addMimeMapping("m4a", "audio/mp4");

    request.setMethod("GET");
    request.setURI(DUMMY_SERVLET + "example.txt");
    request.setVersion(HttpVersion.HTTP_1_0);
  }

  @After
  public void tearDown() throws Exception {
    servletTester.stop();
  }

  @Test
  public void servesFilesMappedToRoot() throws Exception {
    response = makeRequest(ROOT_SERVLET + "assets/example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .isEqualTo("HELLO THERE");
  }

  @Test
  public void servesCharset() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
            .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);

    response = makeRequest(NOCHARSET_SERVLET + "example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.get(HttpHeader.CONTENT_TYPE))
            .isEqualTo(MimeTypes.Type.TEXT_PLAIN.toString());
  }

  @Test
  public void servesFilesFromRootsWithSameName() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "example2.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .isEqualTo("HELLO THERE 2");
  }

  @Test
  public void servesFilesWithA200() throws Exception {
    response = makeRequest();
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .isEqualTo("HELLO THERE");
  }

  @Test
  public void throws404IfTheAssetIsMissing() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "doesnotexist.txt");
    assertThat(response.getStatus())
            .isEqualTo(404);
  }

  @Test
  public void consistentlyAssignsETags() throws Exception {
    response = makeRequest();
    final String firstEtag = response.get(HttpHeaders.ETAG);

    response = makeRequest();
    final String secondEtag = response.get(HttpHeaders.ETAG);

    assertThat(firstEtag)
            .isEqualTo("\"174a6dd7325e64c609eab14ab1d30b86\"")
            .isEqualTo(secondEtag);
  }

  @Test
  public void assignsDifferentETagsForDifferentFiles() throws Exception {
    response = makeRequest();
    final String firstEtag = response.get(HttpHeaders.ETAG);

    response = makeRequest(DUMMY_SERVLET + "foo.bar");
    final String secondEtag = response.get(HttpHeaders.ETAG);

    assertThat(firstEtag)
            .isEqualTo("\"174a6dd7325e64c609eab14ab1d30b86\"");
    assertThat(secondEtag.equals("\"26ae56a90cd78c6720c544707d22110b\"")
        || secondEtag.equals("\"7a13c3f9f2be8379b5a2fb77a85e1d10\""));
  }

  @Test
  public void supportsIfNoneMatchRequests() throws Exception {
    response = makeRequest();
    final String correctEtag = response.get(HttpHeaders.ETAG);

    request.setHeader(HttpHeaders.IF_NONE_MATCH, correctEtag);
    response = makeRequest();
    final int statusWithMatchingEtag = response.getStatus();

    request.setHeader(HttpHeaders.IF_NONE_MATCH, correctEtag + "FOO");
    response = makeRequest();
    final int statusWithNonMatchingEtag = response.getStatus();

    assertThat(statusWithMatchingEtag)
            .isEqualTo(304);
    assertThat(statusWithNonMatchingEtag)
            .isEqualTo(200);
  }

  @Test
  public void consistentlyAssignsLastModifiedTimes() throws Exception {
    response = makeRequest();
    final long firstLastModifiedTime = response.getDateField(HttpHeaders.LAST_MODIFIED);

    response = makeRequest();
    final long secondLastModifiedTime = response.getDateField(HttpHeaders.LAST_MODIFIED);

    assertThat(firstLastModifiedTime)
            .isEqualTo(secondLastModifiedTime);
  }

  @Test
  public void supportsByteRangeForMedia() throws Exception {
    response = makeRequest(ROOT_SERVLET + "assets/foo.mp4");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");

    response = makeRequest(ROOT_SERVLET + "assets/foo.m4a");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
  }

  @Test
  public void supportsFullByteRange() throws Exception {
    request.setHeader(HttpHeaders.RANGE, "bytes=0-");
    response = makeRequest(ROOT_SERVLET + "assets/example.txt");
    assertThat(response.getStatus()).isEqualTo(206);
    assertThat(response.getContent()).isEqualTo("HELLO THERE");
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
            "bytes 0-10/11");
  }

  @Test
  public void supportsCentralByteRange() throws Exception {
    request.setHeader(HttpHeaders.RANGE, "bytes=4-8");
    response = makeRequest(ROOT_SERVLET + "assets/example.txt");
    assertThat(response.getStatus()).isEqualTo(206);
    assertThat(response.getContent()).isEqualTo("O THE");
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
            "bytes 4-8/11");
    assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("5");
  }

  @Test
  public void supportsFinalByteRange() throws Exception {
    request.setHeader(HttpHeaders.RANGE, "bytes=10-10");
    response = makeRequest(ROOT_SERVLET + "assets/example.txt");
    assertThat(response.getStatus()).isEqualTo(206);
    assertThat(response.getContent()).isEqualTo("E");
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
            "bytes 10-10/11");
    assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("1");

    request.setHeader(HttpHeaders.RANGE, "bytes=-1");
    response = makeRequest();
    assertThat(response.getStatus()).isEqualTo(206);
    assertThat(response.getContent()).isEqualTo("E");
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
            "bytes 10-10/11");
    assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("1");
  }

  @Test
  public void rejectsInvalidByteRanges() throws Exception {
    request.setHeader(HttpHeaders.RANGE, "bytes=test");
    response = makeRequest(ROOT_SERVLET + "assets/example.txt");
    assertThat(response.getStatus()).isEqualTo(416);

    request.setHeader(HttpHeaders.RANGE, "bytes=");
    response = makeRequest();
    assertThat(response.getStatus()).isEqualTo(416);

    request.setHeader(HttpHeaders.RANGE, "bytes=1-infinity");
    response = makeRequest();
    assertThat(response.getStatus()).isEqualTo(416);

    request.setHeader(HttpHeaders.RANGE, "test");
    response = makeRequest();
    assertThat(response.getStatus()).isEqualTo(416);
  }

  @Test
  public void supportsMultipleByteRanges() throws Exception {
    request.setHeader(HttpHeaders.RANGE, "bytes=0-0,-1");
    response = makeRequest(ROOT_SERVLET + "assets/example.txt");
    assertThat(response.getStatus()).isEqualTo(206);
    assertThat(response.getContent()).isEqualTo("HE");
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
            "bytes 0-0,10-10/11");
    assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("2");

    request.setHeader(HttpHeaders.RANGE, "bytes=5-6,7-10");
    response = makeRequest();
    assertThat(response.getStatus()).isEqualTo(206);
    assertThat(response.getContent()).isEqualTo(" THERE");
    assertThat(response.get(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    assertThat(response.get(HttpHeaders.CONTENT_RANGE)).isEqualTo(
            "bytes 5-6,7-10/11");
    assertThat(response.get(HttpHeaders.CONTENT_LENGTH)).isEqualTo("6");
  }

  @Test
  public void supportsIfRangeMatchRequests() throws Exception {
    response = makeRequest();
    final String correctEtag = response.get(HttpHeaders.ETAG);

    request.setHeader(HttpHeaders.RANGE, "bytes=10-10");

    request.setHeader(HttpHeaders.IF_RANGE, correctEtag);
    response = makeRequest();
    final int statusWithMatchingEtag = response.getStatus();

    request.setHeader(HttpHeaders.IF_RANGE, correctEtag + "FOO");
    response = makeRequest();
    final int statusWithNonMatchingEtag = response.getStatus();

    assertThat(statusWithMatchingEtag).isEqualTo(206);
    assertThat(statusWithNonMatchingEtag).isEqualTo(200);
  }

  @Test
  public void supportsIfModifiedSinceRequests() throws Exception {
    response = makeRequest();
    final long lastModifiedTime = response.getDateField(HttpHeaders.LAST_MODIFIED);

    request.putDateField(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime);
    response = makeRequest();
    final int statusWithMatchingLastModifiedTime = response.getStatus();

    request.putDateField(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime - 100);
    response = makeRequest();
    final int statusWithStaleLastModifiedTime = response.getStatus();

    request.putDateField(HttpHeaders.IF_MODIFIED_SINCE, lastModifiedTime + 100);
    response = makeRequest();
    final int statusWithRecentLastModifiedTime = response.getStatus();

    assertThat(statusWithMatchingLastModifiedTime)
            .isEqualTo(304);
    assertThat(statusWithStaleLastModifiedTime)
            .isEqualTo(200);
    assertThat(statusWithRecentLastModifiedTime)
            .isEqualTo(304);
  }

  @Test
  public void guessesMimeTypes() throws Exception {
    response = makeRequest();
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
            .isEqualTo(MimeTypes.Type.TEXT_PLAIN_UTF_8);
  }

  @Test
  public void defaultsToHtml() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "foo.bar");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(MimeTypes.CACHE.get(response.get(HttpHeader.CONTENT_TYPE)))
            .isEqualTo(MimeTypes.Type.TEXT_HTML_UTF_8);
  }

  @Test
  public void servesIndexFilesByDefault() throws Exception {
    // Root directory listing:
    response = makeRequest(DUMMY_SERVLET);
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .contains("/assets Index File");

    // Subdirectory listing:
    response = makeRequest(DUMMY_SERVLET + "some_directory");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .contains("/assets/some_directory Index File");

    // Subdirectory listing with slash:
    response = makeRequest(DUMMY_SERVLET + "some_directory/");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .contains("/assets/some_directory Index File");
  }

  @Test
  public void throwsA404IfNoIndexFileIsDefined() throws Exception {
    // Root directory listing:
    response = makeRequest(NOINDEX_SERVLET + '/');
    assertThat(response.getStatus())
            .isEqualTo(404);

    // Subdirectory listing:
    response = makeRequest(NOINDEX_SERVLET + "some_directory");
    assertThat(response.getStatus())
            .isEqualTo(404);

    // Subdirectory listing with slash:
    response = makeRequest(NOINDEX_SERVLET + "some_directory/");
    assertThat(response.getStatus())
            .isEqualTo(404);
  }

  @Test
  public void doesNotAllowOverridingUrls() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "file:/etc/passwd");
    assertThat(response.getStatus())
            .isEqualTo(404);
  }

  @Test
  public void doesNotAllowOverridingPaths() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "/etc/passwd");
    assertThat(response.getStatus())
            .isEqualTo(404);
  }

  @Test
  public void allowsEncodedAssetNames() throws Exception {
    response = makeRequest(DUMMY_SERVLET + "encoded%20example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
  }

  @Test
  public void addMimeMappings() throws Exception {
    response = makeRequest(MIME_SERVLET + "foo.bar");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.get(HttpHeader.CONTENT_TYPE))
            .isEqualTo("application/bar");
  }

  @Test
  public void overrideMimeMapping() throws Exception {
    response = makeRequest(MIME_SERVLET + "example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.get(HttpHeader.CONTENT_TYPE))
            .isEqualTo("application/foo");
  }

  @Test
  public void servesFromMultipleMappings() throws Exception {
    response = makeRequest(MM_ASSET_SERVLET + "/example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .isEqualTo("HELLO THERE");

    response = makeRequest(MM_JSON_SERVLET + "/example.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
    assertThat(response.getContent())
            .isEqualTo("HELLO JSON");
  }

  @Test
  public void noPollutionAcrossMultipleMappings() throws Exception {
    response = makeRequest(MM_ASSET_SERVLET + "/json%20only.txt");
    assertThat(response.getStatus())
            .isEqualTo(404);

    response = makeRequest(MM_JSON_SERVLET + "/json%20only.txt");
    assertThat(response.getStatus())
            .isEqualTo(200);
  }

  @Test
  public void noCacheControlHeaderByDefault() throws Exception {
    response = makeRequest();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.get(HttpHeader.CACHE_CONTROL)).isNull();
  }

  @Test
  public void servesCacheControlHeader() throws Exception {
    response = makeRequest(CACHE_SERVLET + "example.txt");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.get(HttpHeader.CACHE_CONTROL)).isEqualTo("public");
  }
}
