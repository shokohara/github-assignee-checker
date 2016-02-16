package com.github.skohar.githubassigneechecker

import org.scalatest.FlatSpec

class MySpec extends FlatSpec {
  "a" should "not equals to b" in {
    assert("a" !== "b")
  }
}
