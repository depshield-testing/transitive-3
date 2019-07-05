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

  private void bumpVersionProperty(String filename, String propertyName, String fromVersion, String toVersion)
      throws IOException
  {
    if (StringUtils.isBlank(filename)) {
      return;
    }

    File file = new File(filename);

    String text = getText(file);
    String bumped = text;

    String parentNode = extractParent(text);
    String propertiesNode = extractProperties(text);
    String realVersion = extractProperty(propertiesNode, propertyName);
    if (StringUtils.isBlank(realVersion)) {
      bumpVersionProperty(extractRelativePath(file, parentNode), propertyName, fromVersion, toVersion);
    } else if (realVersion.equals(fromVersion)) {
      String newProperties = propertiesNode.replace(
          buildElement(propertyName, fromVersion),
          buildElement(propertyName, toVersion)
      );
      bumped = text.replace(propertiesNode, newProperties);
    }

    if (!bumped.equals(text)) {
      write(filename, bumped);
    } else {
      // check parent
      bumpVersionProperty(extractRelativePath(file, parentNode), propertyName, fromVersion, toVersion);
    }
  }

  public void bump(String filename, String group, String artifact, String fromVersion, String toVersion)
      throws IOException
  {
    if (StringUtils.isBlank(filename)) {
      return;
    }

    File file = new File(filename);
    String text = getText(file);
    String bumped = text;

    String parentNode = extractParent(text);
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

          if (StringUtils.isBlank(realVersion)) {
            bumpVersionProperty(extractRelativePath(file, parentNode), propName, fromVersion, toVersion);
            break;
          } else if (realVersion.equals(fromVersion)) {
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
      write(filename, bumped);
    } else {
      // check parent
      bump(extractRelativePath(file, parentNode), group, artifact, fromVersion, toVersion);
    }
  }

  private void write(String filename, String text) throws IOException {
    File file = new File(filename);
    CharSink sink = Files.asCharSink(file, Charsets.UTF_8);
    sink.write(text);
  }

  private String buildElement(String name, String value) {
    return String.format("<%s>%s</%s>", name, value, name);
  }
  private String getText(File file) throws IOException {
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

  private String extractRelativePath(File file, String source) {
    String path = extractSection(0, source, "<relativePath>", "</relativePath>");
    if (StringUtils.isNotBlank(path)) {
      File parentFile = file;
      while (path.startsWith("..")) {
        parentFile = parentFile.getParentFile();
        path = path.substring(3);
      }
      path = parentFile.getParent() + File.separatorChar + path;
    } else {
      path = file.getParentFile().getParent() + File.separatorChar + "pom.xml";
    }
    return path;
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
