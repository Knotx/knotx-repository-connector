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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.Sets;
import io.knotx.junit5.util.RequestUtil;
import io.knotx.server.api.context.ClientRequest;
import io.knotx.server.api.context.RequestEvent;
import io.knotx.server.api.handler.RequestEventHandlerResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import java.util.Arrays;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class HttpRepositoryConnectorTest {

  @Mock
  private ClientRequest clientRequest;

  private RequestEvent requestEvent;
  private WireMockServer wireMockServer;
  private HttpRepositoryOptions httpRepositoryOptions;

  @BeforeEach
  void setUp() {
    requestEvent = new RequestEvent(clientRequest);
    this.wireMockServer = new WireMockServer(options().dynamicPort());
    this.wireMockServer.start();

    httpRepositoryOptions = new HttpRepositoryOptions();
    httpRepositoryOptions.setClientDestination(new ClientDestination()
        .setScheme("http")
        .setPort(wireMockServer.port())
        .setDomain("localhost"));
  }

  @Test
  void process_whenPathNotExists_expectNotFoundStatus(VertxTestContext testContext, Vertx vertx) {
    //given
    final String requestPath = "/non-existing-template.html";
    when(clientRequest.getPath()).thenReturn(requestPath);
    when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());

    wireMockServer.stubFor(get(urlEqualTo(requestPath))
        .willReturn(aResponse()
            .withHeader("Content-Type", "text/html")
            .withStatus(HttpResponseStatus.NOT_FOUND.code())));

    //when
    HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
    Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

    //then
    RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
        result -> {
          assertEquals(HttpResponseStatus.NOT_FOUND.code(), result.getStatusCode().intValue());
          this.wireMockServer.stop();
        }
    );
  }

  @Test
  void process_whenTemplateExists_expectContentInBodyAndOkStatus(VertxTestContext testContext,
      Vertx vertx) {
    //given
    final String requestPath = "/test-template.html";
    final String body = "This template exists!";
    when(clientRequest.getPath()).thenReturn(requestPath);
    when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());

    wireMockServer.stubFor(get(urlEqualTo(requestPath))
        .willReturn(aResponse()
            .withHeader("Content-Type", "text/html")
            .withHeader("TestName", "Test Value")
            .withStatus(HttpResponseStatus.OK.code())
            .withBody(body)));

    //when
    HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
    Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

    //then
    RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
        result -> {
          assertEquals(HttpResponseStatus.OK.code(), result.getStatusCode().intValue());

          assertTrue(result.getRequestEvent().isPresent());
          assertEquals(body, result.getBody().toString());
          assertEquals("text/html", result.getHeaders().get("Content-Type"));
          assertEquals("Test Value", result.getHeaders().get("TestName"));
          this.wireMockServer.stop();
        }
    );
  }

  @Test
  void process_whenEmptyTemplate_expectStatusOKAndEmptyBody(VertxTestContext testContext,
      Vertx vertx) {
    //given
    final String requestPath = "/empty-body.html";
    when(clientRequest.getPath()).thenReturn(requestPath);
    when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());

    wireMockServer.stubFor(get(urlEqualTo(requestPath))
        .willReturn(aResponse()
            .withHeader("Content-Type", "text/html")
            .withStatus(HttpResponseStatus.OK.code())
            .withBody(StringUtils.EMPTY)));

    //when
    HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
    Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

    //then
    RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
        result -> {
          assertEquals(HttpResponseStatus.OK.code(), result.getStatusCode().intValue());

          assertTrue(result.getRequestEvent().isPresent());
          assertTrue(result.getBody().toString().isEmpty());
          assertEquals("text/html", result.getHeaders().get("Content-Type"));
          this.wireMockServer.stop();
        }
    );
  }

  @Test
  void process_whenRedirectAndFollowRedirect_expectOKStatusAndBodyAndHeadersProperlyPassed(VertxTestContext testContext,
      Vertx vertx) {
    //given
    final String requestPath = "/redirect.html";
    when(clientRequest.getPath()).thenReturn(requestPath);
    when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());

    wireMockServer.stubFor(get(urlEqualTo(requestPath))
        .willReturn(aResponse()
            .withStatus(HttpResponseStatus.MOVED_PERMANENTLY.code())
            .withHeader("location", "/other.html")));

    wireMockServer.stubFor(get(urlEqualTo("/other.html"))
        .willReturn(aResponse()
            .withStatus(HttpResponseStatus.OK.code())
            .withBody("Response from other")));

    clientRequest.getHeaders().add("Host", "www.example.com").add("Test", "123");
    httpRepositoryOptions.setAllowedRequestHeaders(Sets.newHashSet("Host", "Test"));

    //when
    HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
    Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

    //then
    RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
        result -> {
          assertTrue(result.getRequestEvent().isPresent());
          assertEquals(HttpResponseStatus.OK.code(),
              result.getStatusCode().intValue());
          assertEquals("Response from other", result.getBody().toString());
          wireMockServer.getAllServeEvents().forEach(serveEvent -> {
            assertEquals("www.example.com", serveEvent.getRequest().getHeader("Host"));
            assertEquals("123", serveEvent.getRequest().getHeader("Test"));
          });
          this.wireMockServer.stop();
        }
    );
  }

  @Test
  void process_whenRedirectAndNoFollowRedirect_expectRedirectAndNoBody(VertxTestContext testContext,
      Vertx vertx) {
    //given
    final String requestPath = "/redirect.html";
    when(clientRequest.getPath()).thenReturn(requestPath);
    when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());

    wireMockServer.stubFor(get(urlEqualTo(requestPath))
        .willReturn(aResponse()
            .withStatus(HttpResponseStatus.MOVED_PERMANENTLY.code())
            .withHeader("location", "/other.html")));
    httpRepositoryOptions.getClientOptions().setFollowRedirects(false);

    //when
    HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
    Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

    //then
    RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
        result -> {
          assertTrue(result.getRequestEvent().isPresent());
          assertEquals(HttpResponseStatus.MOVED_PERMANENTLY.code(),
              result.getStatusCode().intValue());
          assertTrue(result.getBody().toString().isEmpty());
          this.wireMockServer.stop();
        }
    );
  }

  @Test
  void process_whenServerError_expectServerErrorStatusAndEmptyBody(VertxTestContext testContext,
      Vertx vertx) {
    //given
    final String requestPath = "/500.html";
    when(clientRequest.getPath()).thenReturn(requestPath);
    when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());

    wireMockServer.stubFor(get(urlEqualTo(requestPath))
        .willReturn(aResponse()
            .withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())));

    //when
    HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
    Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

    //then
    RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
        result -> {
          assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
              result.getStatusCode().intValue());
          assertFalse(result.getRequestEvent().isPresent());
          this.wireMockServer.stop();
        }
    );
  }

    @Test
    void process_whenMultipleParam_expectAllValuesArePassed(VertxTestContext testContext, Vertx vertx) {
        //given
        final String requestPath = "/test-template.html";
        when(clientRequest.getPath()).thenReturn(requestPath);
        when(clientRequest.getHeaders()).thenReturn(MultiMap.caseInsensitiveMultiMap());
        when(clientRequest.getParams()).thenReturn(MultiMap.caseInsensitiveMultiMap()
                .add("pets[]", Arrays.<String>asList("dog", "cat"))
                .add("id", Collections.<String>singletonList("someID")));

        wireMockServer.stubFor(get(urlEqualTo(requestPath + "?pets%5B%5D=dog&pets%5B%5D=cat&id=someID"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/html")
                        .withHeader("TestName", "Test Value")
                        .withStatus(HttpResponseStatus.OK.code())));

        //when
        HttpRepositoryConnector connector = new HttpRepositoryConnector(vertx, httpRepositoryOptions);
        Single<RequestEventHandlerResult> connectorResult = connector.process(requestEvent);

        //then
        RequestUtil.subscribeToResult_shouldSucceed(testContext, connectorResult,
                result -> {
                    assertEquals(HttpResponseStatus.OK.code(), result.getStatusCode().intValue());
                    assertEquals("text/html", result.getHeaders().get("Content-Type"));
                    assertEquals("Test Value", result.getHeaders().get("TestName"));
                    assertTrue(result.getRequestEvent().isPresent());

                    MultiMap params = result.getRequestEvent().get().getClientRequest().getParams();
                    assertEquals(2, params.size());
                    assertEquals(2, params.getAll("pets[]").size());
                    assertEquals(1, params.getAll("id").size());

                    this.wireMockServer.stop();
                }
        );
    }
}
