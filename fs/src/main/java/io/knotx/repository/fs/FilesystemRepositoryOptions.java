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
package io.knotx.repository.fs;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Filesystem repository connector configuration
 */
@DataObject(generateConverter = true, publicConverter = false)
public class FilesystemRepositoryOptions {

  /**
   * Default root folder of the filesystem repository
   */
  public final String DEFAULT_CATALOGUE = StringUtils.EMPTY;

  private String catalogue;

  public FilesystemRepositoryOptions() {
    init();
  }

  public FilesystemRepositoryOptions(FilesystemRepositoryOptions other) {
    this.catalogue = other.catalogue;
  }

  public FilesystemRepositoryOptions(JsonObject json) {
    init();
    FilesystemRepositoryOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    FilesystemRepositoryOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    catalogue = DEFAULT_CATALOGUE;
  }

  /**
   * @return Root folder of the files on filesystem repository
   */
  public String getCatalogue() {
    return catalogue;
  }

  /**
   * Set the root folder of the repository on filesystem.
   * If {@code catalogue} is an empty string, a connector will look for the files in the classpath.
   *
   * @param catalogue a root path to the repository files
   * @return a reference to this, so the API can be used fluently
   */
  public FilesystemRepositoryOptions setCatalogue(String catalogue) {
    this.catalogue = catalogue;
    return this;
  }
}
