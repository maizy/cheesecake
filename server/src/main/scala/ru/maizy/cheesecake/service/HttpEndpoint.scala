package ru.maizy.cheesecake.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.net.InetAddress
import ru.maizy.cheesecake.Headers

sealed trait HttpAddress {
  def hostName: String
}
case class IpAddress(ip: InetAddress) extends HttpAddress {
  override def hostName: String = ip.getHostAddress
}
case class SymbolicAddress(host: String) extends HttpAddress {
  override def hostName: String = host
}

case class HttpEndpoint(
    address: HttpAddress,
    port: Int,
    path: String = "/",
    headers: Option[Headers] = None
) extends Endpoint
