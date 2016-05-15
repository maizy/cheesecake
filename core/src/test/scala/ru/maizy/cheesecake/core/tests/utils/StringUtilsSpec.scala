package ru.maizy.cheesecake.core.tests.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.core.tests.BaseSpec
import ru.maizy.cheesecake.core.utils.StringUtils

class StringUtilsSpec extends BaseSpec with FlatSpecLike {

  "tuplesToMultiMap" should "work" in {
    StringUtils.upperCaseToDashes("MyType") shouldBe "my-type"
    StringUtils.upperCaseToDashes("HTTPClient") shouldBe "http-client"
    StringUtils.upperCaseToDashes("CSS") shouldBe "css"
    StringUtils.upperCaseToDashes("doIt") shouldBe "do-it"
    StringUtils.upperCaseToDashes("justDoIT") shouldBe "just-do-it"
    StringUtils.upperCaseToDashes("justDoITAgain") shouldBe "just-do-it-again"
    StringUtils.upperCaseToDashes("alacS") shouldBe "alac-s"
    StringUtils.upperCaseToDashes("simple") shouldBe "simple"
    StringUtils.upperCaseToDashes("simple-a") shouldBe "simple-a"
    StringUtils.upperCaseToDashes("NotSimple-a") shouldBe "not-simple-a"
    StringUtils.upperCaseToDashes("-CSS") shouldBe "-css"
  }

  // TODO
  it should "work with issues for known cases" in {
    StringUtils.upperCaseToDashes("a-HTTPPort") shouldBe "a--http-port"  // should be "a-http-port"
    StringUtils.upperCaseToDashes("-do-not-fix-me") shouldBe "do-not-fix-me"  // should be "-do-not-fix-me"
  }
}
