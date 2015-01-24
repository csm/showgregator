package org.showgregator.service.model

import com.websudos.phantom.zookeeper.{CassandraConnector, SimpleCassandraConnector}

trait Connector extends SimpleCassandraConnector {
  val keySpace = "showgregator"
}
