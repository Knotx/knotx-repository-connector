/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.repository.http;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Describes a configuration of Http Repository connector
 */
@DataObject(generateConverter = true, publicConverter = false)
public class HttpRepositoryOptions {

  private WebClientOptions clientOptions;
  private ClientDestination clientDestination;
  private Set<String> allowedRequestHeaders;
  private List<Pattern> allowedRequestHeaderPatterns;

  private CustomHttpHeaderOptions customHttpHeader;

  /**
   * Default constructor
   */
  public HttpRepositoryOptions() {
    init();
  }

  /**
   * Copy constructor
   *
   * @param other the instance to copy
   */
  public HttpRepositoryOptions(HttpRepositoryOptions other) {
    this.clientOptions = new WebClientOptions(other.clientOptions);
    this.clientDestination = null;
    this.allowedRequestHeaders = new HashSet<>(other.allowedRequestHeaders);
    this.allowedRequestHeaderPatterns = new ArrayList<>(other.allowedRequestHeaderPatterns);
    this.customHttpHeader = new CustomHttpHeaderOptions(other.customHttpHeader);
  }

  /**
   * Create an settings from JSON
   *
   * @param json the JSON
   */
  public HttpRepositoryOptions(JsonObject json) {
    init();
    HttpRepositoryOptionsConverter.fromJson(json, this);
    if (allowedRequestHeaders != null) {
      allowedRequestHeaderPatterns = allowedRequestHeaders.stream()
          .map(expr -> Pattern.compile(expr, Pattern.CASE_INSENSITIVE))
          .collect(Collectors.toList());
    }
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    HttpRepositoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    clientOptions = new WebClientOptions();
    clientDestination = new ClientDestination();
    allowedRequestHeaders = new HashSet<>();
    customHttpHeader = null;
  }

  /**
   * @return {@link WebClientOptions}
   */
  public WebClientOptions getClientOptions() {
    return clientOptions;
  }

  /**
   * Set the {@link WebClientOptions} used by the HTTP client to communicate with remote http
   * repository
   *
   * @param clientOptions {@link WebClientOptions} object
   * @return a reference to this, so the API can be used fluently
   */
  public HttpRepositoryOptions setClientOptions(WebClientOptions clientOptions) {
    this.clientOptions = clientOptions;
    return this;
  }

  /**
   * @return {@link ClientDestination}
   */
  public ClientDestination getClientDestination() {
    return clientDestination;
  }

  /**
   * Set the remote location of the repository
   *
   * @param clientDestination a {@link ClientDestination} object
   * @return a reference to this, so the API can be used fluently
   */
  public HttpRepositoryOptions setClientDestination(ClientDestination clientDestination) {
    this.clientDestination = clientDestination;
    return this;
  }

  /**
   * @return Set of allowed headers patterns
   */
  public Set<String> getAllowedRequestHeaders() {
    return allowedRequestHeaders;
  }

  /**
   * Set the collection of patterns of allowed request headers. Only headers matching any of the
   * pattern from the set will be sent to the HTTP repository
   *
   * @param allowedRequestHeaders a Set of patterns of allowed request headers
   * @return a reference to this, so the API can be used fluently
   */
  public HttpRepositoryOptions setAllowedRequestHeaders(Set<String> allowedRequestHeaders) {
    this.allowedRequestHeaders = allowedRequestHeaders;
    return this;
  }

  /**
   * @return a Custom Header to be sent in every request to the remote repository
   */
  public CustomHttpHeaderOptions getCustomHttpHeader() {
    return customHttpHeader;
  }

  /**
   * Set the header (name and value) to be sent in every request to the remote repository
   *
   * @param customHttpHeader the header name and value
   * @return a reference to this, so the API can be used fluently
   */
  public HttpRepositoryOptions setCustomHttpHeader(CustomHttpHeaderOptions customHttpHeader) {
    this.customHttpHeader = customHttpHeader;
    return this;
  }

  @GenIgnore
  public List<Pattern> getAllowedRequestHeadersPatterns() {
    return allowedRequestHeaderPatterns;
  }

  @GenIgnore
  public HttpRepositoryOptions setAllowedRequestHeaderPatterns(
      List<Pattern> allowedRequestHeaderPatterns) {
    this.allowedRequestHeaderPatterns = allowedRequestHeaderPatterns;
    return this;
  }
}
