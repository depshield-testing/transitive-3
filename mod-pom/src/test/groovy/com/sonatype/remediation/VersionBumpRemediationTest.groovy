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
      File file = new File(getClass().getResource('simple-version-pom.xml').toURI())
      def text = Files.asCharSource(file, Charsets.UTF_8).read()

    when:
      remediator.bump(file.absolutePath, 'org.apache.commons', 'commons-lang3', '3.8', '3.9')
      File file2 = new File(getClass().getResource('simple-version-pom.xml').toURI())
      def text2 = Files.asCharSource(file, Charsets.UTF_8).read()
      // restore the text file
      Files.asCharSink(file, Charsets.UTF_8).write(text)

    then:
      !text2.equals(text)

  }
}
