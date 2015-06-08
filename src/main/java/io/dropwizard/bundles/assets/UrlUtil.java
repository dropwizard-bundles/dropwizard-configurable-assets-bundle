package io.dropwizard.bundles.assets;

import io.dropwizard.servlets.assets.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class UrlUtil {
  /*
   * Separator between JAR URL and file path within the JAR
   */
  private static final String JAR_URL_SEPARATOR = "!/";

  /**
   * Returns true if the URL passed to it corresponds to a directory.
   * This is slightly tricky due to some quirks of the {@link java.util.jar.JarFile} API.
   * Only jar:// and file:// URLs are supported.
   *
   * @param resourceUrl the URL to check
   * @return true if resource is a directory
   */
  public static URL switchFromZipToJarProtocolIfNeeded(URL resourceUrl)
      throws MalformedURLException {
    final String protocol = resourceUrl.getProtocol();
    // If zip protocol switch to jar protocol
    if ("zip".equals(protocol)
        && resourceUrl.getPath().contains(".jar" + JAR_URL_SEPARATOR)) {
      String filePath = resourceUrl.getFile();
      if (!filePath.startsWith("/")) {
        filePath = "/" + filePath;
      }
      return new URL("jar:file:" + filePath);
    }
    return resourceUrl;
  }


}