package com.example.akka.actor

import akka.actor.{Actor, ActorSystem, Props, RootActorPath}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.MemberUp
import com.example.akka.actor.BackEnd.Add
import com.example.akka.actor.FrontEnd.RegisterBackend
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object BackEnd {
  sealed trait RequestMessage
  case class Add(firstNumber:Int, secondNumber:Int) extends RequestMessage

  private val logger = LoggerFactory getLogger BackEnd.getClass

  def initialise(portNumber: Int) = {
    logger info s"Initialising BackEnd actor with port number $portNumber"
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$portNumber")
      .withFallback(ConfigFactory.load().getConfig("Backend"))
    val system = ActorSystem("ClusterSystem", config)
    val Backend = system.actorOf(Props[BackEnd], name = "Backend")
  }
}

class BackEnd extends Actor {

  private val logger = LoggerFactory getLogger BackEnd.getClass

  // Return a cluster identified by supplied implicit actor system
  val cluster: Cluster = Cluster(context.system)

  /**
    * When the backend joins the cluster, it needs to detect front end nodes
    * and send them a registration message, so that they are informed of this backend node.
    * In order to accomplish this, we subscribe to 'MemberUp' cluster change event.
    */
  override def preStart() = {
    cluster.subscribe(self, classOf[MemberUp])
    super.preStart()
  }

  override def postStop() = {
    cluster.unsubscribe(self)
    super.postStop()
  }

  override def receive: Receive = {
    case MemberUp(member) => if ( member hasRole "frontend" )
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") ! RegisterBackend
    case Add(firstOperand, secondOperand) =>
      logger info s"Sum of $firstOperand and $secondOperand is ${firstOperand+ secondOperand}"
    case message =>
      logger warn s"Unsupported message: $message"
  }
}
