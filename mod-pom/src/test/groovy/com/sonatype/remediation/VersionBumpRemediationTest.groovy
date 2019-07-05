/*
 * Copyright (c) 2017-present Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://links.sonatype.com/products/nexus/attributions.
 * "Sonatype" is a trademark of Sonatype, Inc.
 */
package com.sonatype.remediation

import com.google.common.base.Charsets
import com.google.common.io.Files
import spock.lang.Specification

class VersionBumpRemediationTest
  extends Specification
{

  def 'test simple version bump'() {
    setup:
      def remediator = new VersionBumpRemediator()
      File originalFile = getFile('simple-version-pom.xml')
      def originalText = readFile(originalFile)

    when:
      remediator.bump(originalFile.absolutePath, 'org.apache.commons', 'commons-lang3', '3.8', '3.9')
      def bumpedText = readFile('simple-version-pom.xml')
      restoreFile('simple-version-pom.xml', originalText)

    then:
      !bumpedText.equals(originalText)
      originalText.contains("<version>3.8</version>")
      !originalText.contains("<version>3.9</version>")
      bumpedText.contains("<version>3.9</version>")
      !bumpedText.contains("<version>3.8</version>")
  }

  def 'test version bump where version is in a property, same file'() {
    setup:
      def remediator = new VersionBumpRemediator()
      File originalFile = getFile('simple-version-property-pom.xml')
      def originalText = readFile(originalFile)

    when:
      remediator.bump(originalFile.absolutePath, 'org.apache.commons', 'commons-lang3', '3.8', '3.9')
      def bumpedText = readFile('simple-version-property-pom.xml')
      restoreFile('simple-version-property-pom.xml', originalText)

    then:
      !bumpedText.equals(originalText)
      originalText.contains("<commons.lang3.version>3.8</commons.lang3.version>")
      !originalText.contains("<commons.lang3.version>3.9</commons.lang3.version>")
      bumpedText.contains("<commons.lang3.version>3.9</commons.lang3.version>")
      !bumpedText.contains("<commons.lang3.version>3.8</commons.lang3.version>")
  }

  def 'test version bump where version is defined in parent pom dependency'() {
    setup:
      def remediator = new VersionBumpRemediator()
      File originalChildFile = getFile('child-of-versioned-dependency-pom.xml')
      def originalChildText = readFile(originalChildFile)
      def originalParentText = readFile('parent-with-versioned-dependency-pom.xml')

    when:
      remediator.bump(originalChildFile.absolutePath, 'org.apache.commons', 'commons-lang3', '3.8', '3.9')
      def bumpedChildText = readFile('child-of-versioned-dependency-pom.xml')
      def bumpedParentText = readFile('parent-with-versioned-dependency-pom.xml')
      restoreFile('parent-with-versioned-dependency-pom.xml', originalParentText)

    then:
      bumpedChildText == originalChildText
      originalParentText.contains("<version>3.8</version>")
      bumpedParentText.contains("<version>3.9</version>")
      bumpedParentText != originalParentText
      !bumpedParentText.contains("<version>3.8</version>")
  }

  def 'test version bump where version is defined in parent pom as a dependency with a version property'() {
    setup:
      def remediator = new VersionBumpRemediator()
      File originalChildFile = getFile('child-of-versioned-dependency-via-property-pom.xml')
      def originalChildText = readFile(originalChildFile)
      def originalParentText = readFile('parent-with-versioned-dependency-via-property-pom.xml')

    when:
      remediator.bump(originalChildFile.absolutePath, 'org.apache.commons', 'commons-lang3', '3.8', '3.9')
      def bumpedChildText = readFile('child-of-versioned-dependency-via-property-pom.xml')
      def bumpedParentText = readFile('parent-with-versioned-dependency-via-property-pom.xml')
      restoreFile('parent-with-versioned-dependency-via-property-pom.xml', originalParentText)

    then:
      bumpedChildText == originalChildText
      !originalParentText.contains("<version>3.8</version>")
      !bumpedParentText.contains("<version>3.9</version>")
      originalParentText.contains("<commons.lang3.version>3.8</commons.lang3.version>")
      bumpedParentText.contains("<commons.lang3.version>3.9</commons.lang3.version>")
      bumpedParentText != originalParentText
      !bumpedParentText.contains("<commons.lang3.version>3.8</commons.lang3.version>")
  }

  def 'test version bump where version is defined in parent pom property but dependency is defined in child'() {

  }

  def 'test version defined in dependency in parent pom outside project'() {

  }

  def 'test version defined in a property in a parent pom outside project'() {

  }

  private File getFile(String filename) {
    return new File(getClass().getResource(filename).toURI())
  }

  private String readFile(String filename) {
    return readFile(getFile(filename))
  }

  private String readFile(File file) {
    return Files.asCharSource(file, Charsets.UTF_8).read()
  }

  private void restoreFile(String filename, String text) {
    File file = new File(getClass().getResource(filename).toURI())
    Files.asCharSink(file, Charsets.UTF_8).write(text)
  }
}
