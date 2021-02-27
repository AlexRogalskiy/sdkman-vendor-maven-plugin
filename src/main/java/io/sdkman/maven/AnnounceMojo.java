/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2015-2021 The Sdkman Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sdkman.maven;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.net.URISyntaxException;
import java.util.Map;

import static io.sdkman.maven.infra.ApiEndpoints.ANNOUNCE_ENDPOINT;

/**
 * Posts an announcement
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@Mojo(name = "announce")
public class AnnounceMojo extends BaseMojo {

  /** The hashtag to use (legacy) */
  @Parameter(property = "sdkman.hashtag")
  protected String hashtag;

  /** The URL where the release notes can be found */
  @Parameter(property = "sdkman.release.notes.url")
  protected String releaseNotesUrl;

  @Override
  protected Map<String, String> getPayload() {
    Map<String, String> payload = super.getPayload();
    if (hashtag != null && !hashtag.isEmpty()) payload.put("hashtag", hashtag);
    if (releaseNotesUrl != null && !releaseNotesUrl.isEmpty()) payload.put("url", releaseNotesUrl);
    return payload;
  }

  @Override
  protected HttpEntityEnclosingRequestBase createHttpRequest() {
    try {
      return new HttpPost(createURI(ANNOUNCE_ENDPOINT));
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
