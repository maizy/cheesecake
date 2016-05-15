package ru.maizy.cheesecake.core

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConverters.asScalaSetConverter
import scala.util.{ Failure, Success, Try }
import com.typesafe.config.{ Config, ConfigException, ConfigObject }

object RichTypesafeConfig {

  type WarnMessages = Seq[String]
  case class ConfigLookupResults[T](result: T, warnings: WarnMessages)

  sealed trait ConfigError {
    def errorMessage: String
    override def toString: String = errorMessage
  }
  case class MissingValue(errorMessage: String) extends ConfigError
  case class WrongType(errorMessage: String) extends ConfigError
  case class UnknownError(errorMessage: String, cause: Option[Throwable] = None) extends ConfigError

  private def wrapExceptionToEitherWithEmptyForMissing[T](
      path: String,
      typeName: String,
      empty: T)(getter: String => T): Either[ConfigError, T] = {

    Try(getter(path)) match {
      case Failure(e: ConfigException.Missing) =>
        Right(empty)
      case Failure(e: ConfigException.WrongType) =>
        Left(WrongType(s"value for path `$path` isn't $typeName"))
      case Failure(e: Throwable) =>
        Left(UnknownError(s"unable to get value for path `$path`: $e", Some(e)))
      case Success(c) => Right(c)
    }
  }

  private def wrapExceptionToEither[T](path: String, typeName: String)(getter: String => T): Either[ConfigError, T] = {
    Try(getter(path)) match {
      case Failure(e: ConfigException.Missing) =>
        Left(MissingValue(s"value for path `$path` is missing"))
      case Failure(e: ConfigException.WrongType) =>
        Left(WrongType(s"value for path `$path` isn't $typeName"))
      case Failure(e: Throwable) =>
        Left(UnknownError(s"unable to get value for path `$path`: $e", Some(e)))
      case Success(c) => Right(c)
    }
  }

  implicit class ConfigListImplicits(val config: Config) extends AnyVal {

    @throws[ConfigException]
    def asScalaConfigList(path: String): IndexedSeq[Config] = config.getConfigList(path).toIndexedSeq

    def eitherConfigList(path: String, allowEmpty: Boolean = false): Either[ConfigError, IndexedSeq[Config]] =
      wrapExceptionToEitherWithEmptyForMissing[IndexedSeq[Config]](path, "list", IndexedSeq.empty) { asScalaConfigList }

    def optConfigList(path: String, allowEmpty: Boolean = false): Option[IndexedSeq[Config]] =
      eitherConfigList(path, allowEmpty).right.toOption
  }


  implicit class StringImplicits(val config: Config) extends AnyVal {

    def eitherString(path: String): Either[ConfigError, String] =
      wrapExceptionToEither(path, "string") { config.getString }

    def optString(path: String): Option[String] = eitherString(path).right.toOption
  }


  implicit class IntImplicits(val config: Config) extends AnyVal {

    def eitherInt(path: String): Either[ConfigError, Int] =
      wrapExceptionToEither(path, "int") { config.getInt }

    def optInt(path: String): Option[Int] = eitherInt(path).right.toOption
  }

  implicit class ObjectImplicits(val config: Config) extends AnyVal {

    def eitherObject(path: String): Either[ConfigError, ConfigObject] =
      wrapExceptionToEither(path, "object") { config.getObject }

    def optObject(path: String): Option[ConfigObject] = eitherObject(path).right.toOption

    def eitherStringMapWithWarnings(path: String): ConfigLookupResults[Either[ConfigError, Map[String, String]]] = {
      eitherObject(path) match {
        case Right(obj) =>
          val entiries = obj.entrySet.asScala.toIndexedSeq
          ConfigLookupResults(
            Right(
              entiries
                .filter(_.getValue.unwrapped.isInstanceOf[String])
                .map(e => (e.getKey, e.getValue.unwrapped.asInstanceOf[String]))
                .toMap
            ),
            entiries
              .filterNot(_.getValue.unwrapped.isInstanceOf[String])
              .map(wrongValue => s"Value for `${wrongValue.getKey}` isn't string")
          )
        case Left(error) => ConfigLookupResults(Left(error), Seq.empty)
      }
    }

    def eitherStringMap(path: String): Either[ConfigError, Map[String, String]] =
      eitherStringMapWithWarnings(path).result

    def optStringMapWithWarnings(path: String): ConfigLookupResults[Option[Map[String, String]]] = {
      val eitherMapResults = eitherStringMapWithWarnings(path)
      ConfigLookupResults(eitherMapResults.result.right.toOption, eitherMapResults.warnings)
    }

    def optStringMap(path: String): Option[Map[String, String]] = optStringMapWithWarnings(path).result
  }


}
