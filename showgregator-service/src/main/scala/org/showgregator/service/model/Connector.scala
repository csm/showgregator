package org.showgregator.service.model

import com.websudos.phantom.zookeeper.SimpleCassandraConnector

trait Connector extends SimpleCassandraConnector {
  val keySpace = "showgregator"
}
