package com.truward.brikar.server.test.util;

import com.truward.brikar.server.util.JettyResourceUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

/**
 * @author Alexander Shabanov
 */
public final class JettyResourceUtilTest {
  private final String fileContents = "fileContents";

  @Test
  public void shouldCreateFileResource() throws IOException {
    // Given:
    final String path = createTempFile();

    // When:
    final Resource resource = JettyResourceUtil.createResource("file:" + path);

    // Then:
    assertNotNull(resource);
    final String actualContent = new String(StreamUtils.copyToByteArray(resource.getInputStream()), UTF_8);
    assertEquals(fileContents, actualContent);
  }

  @Test
  public void shouldCreateFileResourceWithoutUsingResourceQualifier() throws IOException {
    // Given:
    final String path = createTempFile();

    // When:
    final Resource resource = JettyResourceUtil.createResource(path);

    // Then:
    assertNotNull(resource);
    final String actualContent = new String(StreamUtils.copyToByteArray(resource.getInputStream()), UTF_8);
    assertEquals(fileContents, actualContent);
  }

  @Test
  public void shouldCreateClassPathResource() throws IOException {
    // Given:
    final String path = "classpath:/spring/default-service-base.xml";

    // When:
    final Resource resource = JettyResourceUtil.createResource(path);

    // Then:
    assertNotNull(resource);
    final String actualContent = new String(StreamUtils.copyToByteArray(resource.getInputStream()), UTF_8);
    assertFalse(actualContent.isEmpty());
  }

  @Test(expected = IOException.class)
  public void shouldNotCreateFileResourceUsingRelativePath() throws IOException {
    JettyResourceUtil.createResource("test" + File.pathSeparator + "sample");
  }

  @Test(expected = IOException.class)
  public void shouldNotCreateResourceUsingUnknownProtocol() throws IOException {
    JettyResourceUtil.createResource("vfs:/test/sample");
  }

  //
  // Private
  //

  private String createTempFile() throws IOException {
    final File tempFile = File.createTempFile("sample", "path");
    try (final FileOutputStream fos = new FileOutputStream(tempFile)) {
      fos.write(fileContents.getBytes(UTF_8));
    }
    tempFile.deleteOnExit();
    return tempFile.getAbsolutePath();
  }
}
