package ru.maizy.cheesecake.server.marshallers

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.cheesecake.server.jsonapi.models.FullView
import ru.maizy.cheesecake.server.resultsstorage.{ Aggregate, AggregateResult }
import ru.maizy.cheesecake.server.service.EndpointFQN
import spray.json.{ JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, pimpAny }

// TODO: how to use only writer for case classes
trait FullViewJsonMarshallers
  extends AggregateResultJsonMarshallers
  with ServiceJsonMarshallers
  with JsonMarshaller
{

  implicit object FullViewFormat extends RootJsonFormat[FullView] {

    def write(fullView: FullView): JsValue = {
      JsObject(
        "services_results" -> JsObject(
          "total_services" -> JsNumber(fullView.resultsGrouped.size),
          "total_endpoints" -> JsNumber(fullView.resultsGrouped.map(_._2.size).sum),
          "services" -> JsArray(
            fullView.resultsGrouped.map {
              case (service, endpointResults) =>
                JsObject(
                  "total_endpoints" -> JsNumber(endpointResults.size),
                  "service" -> service.toJson,
                  "endpoints" -> writeEndpointsResults(endpointResults)
                )
            }.toVector
          )
        )
      )
    }

    private def writeEndpointsResults(results: Map[EndpointFQN, Map[Aggregate, AggregateResult[Any]]]): JsArray = {
      val r = results.map { case (endpointFqn, endpointResults) =>
        JsObject(
          "endpoint" -> endpointFqn.endpoint.toJson,
          "aggregates" -> writeAggregatesResults(endpointResults)
        )
      }.toVector
      JsArray(r)
    }

    private def writeAggregatesResults(aggregates: Map[Aggregate, AggregateResult[Any]]): JsObject =
      JsObject(
        aggregates.map { case (aggregate, value) =>
          aggregate.uniqueKey -> value.toJson
        }
      )

    override def read(json: JsValue): FullView = ???
  }
}
