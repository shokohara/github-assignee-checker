package com.github.skohar.githubassigneechecker

import java.net.InetAddress

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.route53.AmazonRoute53Client
import com.amazonaws.services.route53.model._
import com.amazonaws.services.s3.AmazonS3Client
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.util.control.Exception._
import scalaj.http._

object Main {

  def main(args: Array[String]): Unit = {
    val json = Json.parse(args.head)
    val host = json.\("host").as[String]
    val oAuthToken = json.\("token").as[String]
    val or: Map[String, Seq[String]] = json.\\("repos").map { x =>
      val owner = x.\("owner").as[String]
      val repositories = x.\("repositories").as[List[String]]
      owner -> repositories
    }.toMap
    val or2 = or.toList.flatMap(x => x._2.map(y => (x._1, y)))
    val userId = userId(host, oAuthToken)
    val exists = or2.exists(x => isAssignee(host, oAuthToken, x._1, x._2, userId))
  }

  def isAssignee(host: String, token: String, owner: String, repository: String, userId: Long): Boolean = {
    val url = s"$host/repos/$owner/$repository/pulls"
    val res = Http(url).header("Accept", "application/vnd.github.v3+json").header("Authorization", s"token $token").asString.body
    Json.parse(res).\\("assignee").map(_.\("id").as[Long]).contains(userId)
  }

  def userId(host: String, token: String): Long = {
    val res = Http(s"$host/user").header("Accept", "application/vnd.github.v3+json").header("Authorization", s"token $token").asString.body
    Json.parse(res).\("id").as[Long]
  }
}
