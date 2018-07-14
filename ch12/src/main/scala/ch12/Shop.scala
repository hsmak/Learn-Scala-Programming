package ch12

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.Receptionist._
import ch12.Bakery.Groceries
import ch12.Manager.ReceiveGroceries
import ch12.Shop.{config, seller}
import com.typesafe.config.ConfigFactory

object ShopApp extends App {
  val system = ActorSystem(seller, "Typed-Bakery", config)
}
object Shop {
  final case class ShoppingList(eggs: Int,
                                flour: Int,
                                sugar: Int,
                                chocolate: Int)
  final case class SellByList(list: ShoppingList,
                              toWhom: ActorRef[Manager.Command])

  val SellerKey = ServiceKey[SellByList]("GrocerySeller")
  val config = ConfigFactory.load("grocery.conf")

  val seller: Behavior[SellByList] = Behaviors.setup { ctx ⇒
    ctx.system.receptionist ! Register(SellerKey, ctx.self)
    Behaviors.receiveMessage[SellByList] {
      case SellByList(list, toWhom) ⇒
        import list._
        toWhom ! ReceiveGroceries(Groceries(eggs, flour, sugar, chocolate))
        Behaviors.same
    }
  }

}
