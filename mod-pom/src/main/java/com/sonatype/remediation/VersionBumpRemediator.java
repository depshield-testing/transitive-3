/*
 * Copyright (c) 2017-present Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://links.sonatype.com/products/nexus/attributions.
 * "Sonatype" is a trademark of Sonatype, Inc.
 */
package com.sonatype.remediation;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;

public class VersionBumpRemediator
{

  public void bump(String filename, String group, String artifact, String fromVersion, String toVersion)
      throws IOException
  {
    String text = getText(filename);
    String bumped = text;

    String parentNode = extractParent(text);

    // need properties
    String propertiesNode = extractProperties(text);

    // find matching dependency
    String dependency = "";
    int index = 0;
    do {
      dependency = extractDependency(index, text);
      if (group.equals(extractGroup(dependency)) && artifact.equals(extractArtifact(dependency))) {
        String version = extractVersion(dependency);
        if (version.startsWith("${")) {
          String propName = version.substring(2, version.length() - 1);
          String realVersion = extractProperty(propertiesNode, propName);
          // todo realVersion blank - prop could be in parent file
          if (realVersion.equals(fromVersion)) {
            String newProperties = propertiesNode.replace(
                buildElement(propName, fromVersion),
                buildElement(propName, toVersion)
            );
            bumped = text.replace(propertiesNode, newProperties);
            break;
          }
        } else if (StringUtils.isNotBlank(version) && fromVersion.equals(version)) {
          String newDependency = dependency.replace(
              buildElement("version", fromVersion),
              buildElement("version", toVersion)
          );
          bumped = text.replace(dependency, newDependency);
          break;
        }
      }
      index = text.indexOf(dependency);
    } while (StringUtils.isNotBlank(dependency));

    if (!bumped.equals(text)) {
      File file = new File(filename);
      CharSink sink = Files.asCharSink(file, Charsets.UTF_8);
      sink.write(bumped);
    } else {
      // check parent
      bump(extractRelativePath(parentNode), group, artifact, fromVersion, toVersion);
    }
  }

  private String buildElement(String name, String value) {
    return String.format("<%s>%s</%s>", name, value, name);
  }
  private String getText(String filename) throws IOException {
    File file = new File(filename);
    return Files.asCharSource(file, Charsets.UTF_8).read();
  }

  private String extractArtifact(String source) {
    return extractSection(0, source, "<artifactId>", "</artifactId>");
  }

  private String extractGroup(String source) {
    return extractSection(0, source, "<groupId>", "</groupId>");
  }

  private String extractVersion(String source) {
    return extractSection(0, source, "<version>", "</version>");
  }

  private String extractRelativePath(String source) {
    return extractSection(0, source, "<relativePath>", "</relativePath>");
  }

  private String extractDependency(int fromIndex, String source) {
    return extractSection(fromIndex, source, "<dependency>", "/dependency>");
  }

  private String extractParent(String source) {
    return extractSection(0, source, "<parent>", "</parent>");
  }

  private String extractProperties(String source) {
    return extractSection(0, source, "<properties>", "</properties>");
  }

  private String extractProperty(String source, String propertyName) {
    return extractSection(0, source, "<" + propertyName + ">", "</" + propertyName + ">");
  }

  private String extractSection(int fromIndex, String source, String start, String stop) {
    String result = "";

    int beginIndex = source.indexOf(start, fromIndex);
    if (beginIndex >= 0) {
      int endIndex = source.indexOf(stop, beginIndex);
      result = source.substring(beginIndex + start.length(), endIndex);
    }

    return result;
  }
}
