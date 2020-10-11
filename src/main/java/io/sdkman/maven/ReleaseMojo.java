package io.sdkman.maven;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.sdkman.maven.infra.ApiEndpoints.RELEASE_ENDPOINT;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "release")
public class ReleaseMojo extends BaseMojo {

  @Parameter(property = "sdkman.url")
  protected String url;

  @Parameter(property = "sdkman.platforms")
  protected Map<String, String> platforms;

  @Override
  protected Map<String, String> getPayload() {
    // url is required if platforms is empty
    if ((platforms == null || platforms.isEmpty()) && (url == null || url.isEmpty())) {
      throw new IllegalArgumentException("Missing url");
    }

    Map<String, String> payload = super.getPayload();
    payload.put("url", url);
    return payload;
  }

  @Override
  protected HttpEntityEnclosingRequestBase createHttpRequest() {
    try {
      return new HttpPost(new URI("https", apiHost, RELEASE_ENDPOINT, null));
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void execute() throws MojoExecutionException {
    try {
      HttpResponse resp = executeRelease();
      int statusCode = resp.getStatusLine().getStatusCode();
      if (statusCode < 200 || statusCode >= 300) {
        throw new IllegalStateException("Server returned error " + resp.getStatusLine());
      }
    } catch (Exception e) {
      throw new MojoExecutionException("Sdk release failed", e);
    }
  }

  protected HttpResponse executeRelease() throws IOException {
    if (platforms == null || platforms.isEmpty()) {
      return execCall(getPayload(), createHttpRequest());
    }

    List<HttpResponse> responses = new ArrayList<>();
    for (Map.Entry<String, String> platform : platforms.entrySet()) {
      Map<String, String> payload = super.getPayload();
      payload.put("platform", platform.getKey());
      payload.put("url", platform.getValue());
      responses.add(execCall(payload, createHttpRequest()));
    }

    return responses.stream()
        .filter(resp -> {
          int statusCode = resp.getStatusLine().getStatusCode();
          return statusCode < 200 || statusCode >= 300;
        })
        .findFirst()
        .orElse(responses.get(responses.size() - 1));
  }
}
