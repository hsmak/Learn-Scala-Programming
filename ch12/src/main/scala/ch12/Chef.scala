package ch12

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import ch12.Bakery.{Groceries, Pastry}
import ch12.Manager.ReceivePastry

object Chef {

  sealed trait Command

  final case class Mix(g: Groceries, manager: ActorRef[Manager.Command])
    extends Command

  final case class Collect(p: Pastry, mixer: ActorRef[Mixer.Mix])
    extends Command

  final case class BrokenMixer(mixer: ActorRef[Mixer.Mix]) extends Command

  def idle(mixerFactory: Behavior[Mixer.Mix]): Behaviors.Receive[Command] =
    Behaviors.receivePartial[Command] {
      case (context,
      mix@Mix(Groceries(eggs, flour, sugar, chocolate), manager)) =>
        val mixers = for (i <- 1 to eggs)
          yield
            context.spawn(mixerFactory,
              s"Mixer_$i",
              DispatcherSelector.fromConfig("mixers-dispatcher"))
        mixers.foreach(_ ! Mixer.Mix(mix.g, context.self))
        mixing(mixers.toSet, 0, manager, mixerFactory)
    }

  def mixing(mixers: Set[ActorRef[Mixer.Mix]],
             collected: Int,
             manager: ActorRef[Manager.Command],
             mixerBuilder: Behavior[Mixer.Mix]): Behaviors.Receive[Command] =
    Behaviors.receivePartial {
      case (context, Collect(pastry, mixer)) =>
        val mixersToGo = mixers - mixer
        val pastryBuf = collected + pastry.weight
        context.stop(mixer)
        if (mixersToGo.isEmpty) {
          manager ! ReceivePastry(Pastry(pastryBuf))
          idle(mixerBuilder)
        } else {
          mixing(mixersToGo, pastryBuf, manager, mixerBuilder)
        }
      case (context, BrokenMixer(m)) =>
        context.log.warning("Broken mixer detected {}", m)
        context.self ! Collect(Pastry(0), m)
        Behaviors.same
    }

}
