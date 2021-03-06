/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.harvester.builder

import org.apache.spark.sql.catalyst.expressions.{Attribute => SparAttribute}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import za.co.absa.spline.harvester.ComponentCreatorFactory
import za.co.absa.spline.harvester.builder.OperationNodeBuilder.{OperationId, OutputAttIds}

trait OperationNodeBuilder {

  protected type R

  val id: OperationId = componentCreatorFactory.nextId.toString

  private var childBuilders: Seq[OperationNodeBuilder] = Nil

  def operation: LogicalPlan
  def build(): R
  def +=(childBuilder: OperationNodeBuilder): Unit = childBuilders :+= childBuilder

  protected def componentCreatorFactory: ComponentCreatorFactory
  private lazy val attributeConverter =
    if (isTerminal)
      componentCreatorFactory.inputAttributeConverter
    else
      componentCreatorFactory.expressionConverter

  private def convert(att: SparAttribute): String = attributeConverter.convert((att, id)).id
  protected lazy val outputAttributes: OutputAttIds = operation.output.map(convert).toList

  protected def childIds: Seq[OperationId] = childBuilders.map(_.id)
  protected def childOutputSchemas: Seq[OutputAttIds] = childBuilders.map(_.outputAttributes)
  protected def isTerminal: Boolean = childBuilders.isEmpty
}

object OperationNodeBuilder {
  type OperationId = String
  type OutputAttIds = List[String]
}
