package com.example.akka.application

import com.example.akka.actor.FrontEnd.CalculateSum
import com.example.akka.actor.{BackEnd, FrontEnd}

import scala.language.postfixOps


object ClusterApplication extends App{
  //initiate frontend node
  FrontEnd initialise()

  //initiate three nodes from backend
  BackEnd initialise 2552
  BackEnd initialise 2560
  BackEnd initialise 2561

  Thread sleep 10000

  FrontEnd.instance ! CalculateSum(2, 4)


}
