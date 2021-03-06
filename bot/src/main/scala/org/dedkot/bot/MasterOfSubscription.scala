package org.dedkot.bot

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object MasterOfSubscription {

  def apply(): Behavior[String] =
    Behaviors.setup(context => new MasterOfSubscription(context))

}

class MasterOfSubscription(context: ActorContext[String]) extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] = {
    Behaviors.same
  }

}