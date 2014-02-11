package com.softwaremill.codebrag.dao.mongo

import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{MongoClient, ServerAddress}
import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoRecord
import com.softwaremill.codebrag.dao.followup.FollowupRecord
import com.softwaremill.codebrag.dao.DaoConfig

object MongoInit extends Logging {
  def initialize(daoConfig: DaoConfig) {
    initializeWithoutIndexCheck(daoConfig)
    ensureMongoIndexes()
  }

  def initializeWithoutIndexCheck(daoConfig: DaoConfig) {
    MongoDB.defineDb(DefaultMongoIdentifier, createMongo(asServerAdresses(daoConfig.mongoServers)), daoConfig.mongoDatabase)
  }

  def ensureMongoIndexes() {
    logger.info("Ensuring Mongo indexes")
    CommitInfoRecord.ensureIndexes()
    FollowupRecord.ensureIndexes()
  }

  private def createMongo(serverList: List[ServerAddress]) = {
    // We need to use a different constructor if there's only 1 server to avoid startup exceptions where Mongo thinks
    // it's in a replica set.
    if (serverList.size == 1) {
      new MongoClient(serverList.head)
    } else {
      import JavaConversions._
      new MongoClient(serverList)
    }
  }

  private def asServerAdresses(servers: String): scala.List[ServerAddress] = {
    servers.split(",").map(new ServerAddress(_)).toList
  }
}