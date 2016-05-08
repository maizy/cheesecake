package ru.maizy.cheesecake.client

import ru.maizy.cheesecake.api
import ru.maizy.cheesecake.core.utils.CollectionsUtils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class Stub(stub: String, apiDep: api.Stub, coreDep: CollectionsUtils.type)  // FIXME: tmp
