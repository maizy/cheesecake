package ru.maizy.cheesecake.service

/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */


import java.net.InetAddress

sealed trait HttpAddress
case class IpAddress(ip: InetAddress) extends HttpAddress
case class SymbolicAddress(host: String) extends HttpAddress

case class HttpEndpoint(address: HttpAddress, port: Int) extends Endpoint
