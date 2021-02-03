package database

import java.sql.{Connection, DriverManager, SQLException}
import oracle.jdbc.pool.OracleDataSource
import play.api.libs.json._

import java.util.Locale
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Database {

  def parseResults[T](resultSet: java.sql.ResultSet)(f: java.sql.ResultSet => T) = {
    new Iterator[T] {
      def hasNext = resultSet.next()
      def next() = f(resultSet)
    }
  }

  def sqlResultSetToJson(rs: java.sql.ResultSet): JsValue = {
    val rsmd = rs.getMetaData
    val columnCount = rsmd.getColumnCount

    var qJsonArray: JsArray = Json.arr()
    while (rs.next) {
      var index = 1

      var rsJson: JsObject = Json.obj()
      while (index <= columnCount) {

        val column = rsmd.getColumnLabel(index)
        val columnLabel = column.toLowerCase()

        val value = rs.getObject(column)
        if (value == null) {
          rsJson = rsJson ++ Json.obj(
            columnLabel -> JsNull
          )
        } else if (value.isInstanceOf[Integer]) {
//          println(value.asInstanceOf[Integer])
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.asInstanceOf[Int]
          )
        } else if (value.isInstanceOf[String]) {
//          println(value.asInstanceOf[String])
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.asInstanceOf[String]
          )
        } else if (value.isInstanceOf[Boolean]) {
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.asInstanceOf[Boolean]
          )
        } else if (value.isInstanceOf[Long]) {
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.asInstanceOf[Long]
          )
        } else if (value.isInstanceOf[Double]) {
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.asInstanceOf[Double]
          )
        } else if (value.isInstanceOf[Float]) {
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.asInstanceOf[Float]
          )
        } else if (value.isInstanceOf[java.math.BigDecimal]) {
          rsJson = rsJson ++ Json.obj(
            columnLabel -> value.toString
          )
        } else {
          throw new IllegalArgumentException("Unmappable object type: " + value.getClass)
        }
        index += 1
      }
      qJsonArray = qJsonArray :+ rsJson
    }
    qJsonArray
  }

  def getConnection(): java.sql.Connection = {

    Locale.setDefault(Locale.ENGLISH)

    val connection : Connection = null
    val oracleUser = "system"
    val oraclePassword = "oracle"
    val oracleURL = "jdbc:oracle:thin:@//192.168.99.100:49161/xe"

    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)

    val con = ods.getConnection()
    con
  }

  def getAllProducts : Future[JsValue] = Future {

    val query = """
      SELECT * FROM product
        """

    val con = getConnection()
    val statement = con.createStatement()
    val  resultSet : java.sql.ResultSet = statement.executeQuery(query)

    val jsResult = sqlResultSetToJson(resultSet)

    resultSet.close()
    statement.close()
    con.close()

    jsResult

  }


}
