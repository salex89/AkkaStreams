import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult, ThrottleMode}
import akka.util.ByteString
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by aleksandar on 6/26/16.
  */
object Main {

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("QuickStart")
    implicit val materializer = ActorMaterializer()

    val source: Source[Int, NotUsed] = Source(1 to 100)
    val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
    factorials.zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
      .throttle(1, 1 second, 1, ThrottleMode.shaping)
      .runForeach(println)

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer,
      Set("topic1"))
      .withBootstrapServers("localhost:9092")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val client = ServiceClient("http://demo0197015.mockable.io/path")

    Consumer.committableSource(consumerSettings.withClientId("client1"))
      .map(msg => {
        client.execute(msg.value)
        return msg
      }).map()
  }

}
