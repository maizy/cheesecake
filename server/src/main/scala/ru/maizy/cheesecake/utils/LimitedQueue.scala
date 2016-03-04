package ru.maizy.cheesecake.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.annotation.tailrec
import scala.collection.immutable.Queue


object LimitedQueue {

  implicit class LimitedQueueImpl[T](queue: Queue[T]) {
    def enqueueWithLimit[E >: T](elem: E, limit: Int): Queue[E] = {
      if (limit == 0) {
        Queue[T]()
      } else {
        require(limit > 0)
        @tailrec
        def recDequeue(xs: Queue[E]): Queue[E] = {
          if (xs.size <= limit) {
            xs
          } else {
            recDequeue(xs.dequeue._2)
          }
        }

        recDequeue(queue.enqueue(elem))
      }
    }
  }
}

