package ru.maizy.cheesecake.tests.checker

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

import java.net.InetAddress
import org.scalatest.FlatSpecLike
import ru.maizy.cheesecake.service.{ IpAddress, HttpEndpoint }
import ru.maizy.cheesecake.tests.BaseSpec

class HttpEndpointSpec extends BaseSpec with FlatSpecLike {
  val address = IpAddress(InetAddress.getByName("127.0.0.1"))
  val sameAddress = IpAddress(InetAddress.getByName("127.0.0.1"))
  val endpoint = HttpEndpoint(address, 1234)
  val sameEndpoint = HttpEndpoint(address, 1234)
  val sameAddressOtherPortEndpoint = HttpEndpoint(address, 4321)
  val sameAddressSamePortEndpoint = HttpEndpoint(sameAddress, 1234)

  "HttpEndpoint" should "support equality" in {
    (endpoint == sameEndpoint) shouldBe true
    (endpoint == sameAddressSamePortEndpoint) shouldBe true
    (endpoint == sameAddressOtherPortEndpoint) shouldBe false
    (sameEndpoint == sameAddressOtherPortEndpoint) shouldBe false
  }

  it should "has equal hashes" in {
    endpoint.hashCode() shouldBe sameEndpoint.hashCode()
    endpoint.hashCode() shouldBe sameAddressSamePortEndpoint.hashCode()
    endpoint.hashCode() shouldNot be(sameAddressOtherPortEndpoint.hashCode())
    sameEndpoint.hashCode() shouldNot be(sameAddressOtherPortEndpoint.hashCode())
  }
}
