/*
 * Copyright 2018 ABSA Group Limited
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

package za.co.absa.cobrix.spark.cobol.source.integration

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.scalatest.FunSuite
import za.co.absa.cobrix.spark.cobol.source.base.SparkTestBase

//noinspection NameBooleanParameters
class CobolIntegration3Spec extends FunSuite with SparkTestBase {

  private val exampleName = "Test3"
  private val inputCopybookPath = "file://../data/test3_copybook.cob"
  private val inpudDataPath = "../data/test3_data"

  private val expectedSchemaPath = "../data/test3_expected/test3_schema.json"
  private val actualSchemaPath = "../data/test3_expected/test3_schema_actual.json"
  private val expectedResultsPath = "../data/test3_expected/test3.txt"
  private val actualResultsPath = "../data/test3_expected/test3_actual.txt"

  test(s"Integration test on $exampleName data") {
    val df = spark
      .read
      .format("cobol")
      .option("copybook", inputCopybookPath)
      .option("schema_retention_policy", "collapse_root")
      .option("generate_record_id", "true")
      .option("segment_field", "SIGNATURE")
      .option("segment_filter", "S9276511")
      .load(inpudDataPath)

    // This is to print the actual output
    println(df.schema.json)
    //df.toJSON.take(60).foreach(println)

    val expectedSchema = Files.readAllLines(Paths.get(expectedSchemaPath), StandardCharsets.ISO_8859_1).toArray.mkString("\n")
    val actualSchema = df.schema.json

    if (actualSchema != expectedSchema) {
      val writer = new PrintWriter(actualSchemaPath)
      try {
        writer.write(actualSchema)
      } finally {
        writer.close()
      }
      assert(false, s"The actual schema doesn't match what is expected for $exampleName example. Please compare contents of $expectedSchemaPath to " +
        s"$actualSchemaPath for details.")
    }

    val actual = df.toJSON.take(60)
    val expected = Files.readAllLines(Paths.get(expectedResultsPath), StandardCharsets.ISO_8859_1).toArray

    if (!actual.sameElements(expected)) {
      val writer = new PrintWriter(actualResultsPath)
      try {
        for (str <- actual) {
          writer.write(str)
          writer.write("\n")
        }
      } finally {
        writer.close()
      }
      assert(false, s"The actual data doesn't match what is expected for $exampleName example. Please compare contents of $expectedResultsPath to $actualResultsPath for details.")
    }
  }

}
