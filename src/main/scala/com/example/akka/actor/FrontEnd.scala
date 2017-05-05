package com.example.akka.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import com.example.akka.actor.BackEnd.Add
import com.example.akka.actor.FrontEnd.{CalculateSum, RegisterBackend}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.language.postfixOps
import scala.util.Random

object FrontEnd {
  sealed trait RequestMessage
  case object RegisterBackend extends RequestMessage
  // Message to trigger simulation of complex backend processing
  case class CalculateSum(firstNumber: Int, secondNumber: Int) extends RequestMessage

  private val logger = LoggerFactory getLogger FrontEnd.getClass

  private[this] var _frontend: ActorRef = _

  def initialise() = {
    logger info s"Initialising FrontEnd actor"

    val config = ConfigFactory.load().getConfig("Frontend")
    val system = ActorSystem("ClusterSystem", config)
    _frontend = system.actorOf(Props[FrontEnd], name = "frontend")
  }

  def instance = _frontend

}

class FrontEnd extends Actor {

  private val logger = LoggerFactory getLogger FrontEnd.getClass

  var backendNodes = List.empty[ActorRef]

  override def receive: Receive = {
    case RegisterBackend if !(backendNodes contains sender ) =>
      backendNodes = sender :: backendNodes
      context watch sender
    case Terminated => backendNodes = backendNodes filterNot (_ == sender)
    case CalculateSum if backendNodes isEmpty =>
      logger error "Unable to process CalculateSum message as no back-end nodes are available to process the request"
    case CalculateSum(firstOperand, secondOperand) =>
      val backendNodeIndex = Random nextInt backendNodes.size
      logger debug s"Forwarding to backend node with index $backendNodeIndex"
      backendNodes(backendNodeIndex) forward Add(firstOperand, secondOperand)
    case message =>
      logger warn s"Unsupported message: $message"
  }
}
