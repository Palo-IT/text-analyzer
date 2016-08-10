import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.server.blaze._
import org.http4s.server.{Server, ServerApp}

import scala.collection.immutable.Iterable
import scala.util.parsing.json.JSONArray
import scalaz.concurrent.Task

case class WordFreq (word: String, freq: Long) {
  override def toString = "{ \"text\": \"%s\", \"size\": %d }".format(this.word, this.freq)
}

object CirceImplicits {

  implicit val jsonEncoder: EntityEncoder[JSONArray] =
    EntityEncoder
      .stringEncoder(Charset.`UTF-8`)
      .contramap{json: JSONArray => json.toString()}
      .withContentType(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
}

object WordsController {
  LuceneIndex.createIndex()

  def getTopWords(howMany: Int): JSONArray = {
    val wordFreqs: Iterable[WordFreq] = LuceneIndex.topTerms(howMany).map(row => WordFreq(row._1, row._2))
    JSONArray(wordFreqs.toList)
  }

  val wordCloudService = HttpService {
    //TODO: merge static calls OR use a web framework
    case GET -> Root / path =>
      StaticFile.fromResource("/" + path.toString).fold(NotFound())(Task.now)
    case GET -> Root / "js" / path =>
      StaticFile.fromResource("/js/" + path.toString).fold(NotFound())(Task.now)
    case GET -> Root / "js" / "lib" / path =>
      StaticFile.fromResource("/js/lib/" + path.toString).fold(NotFound())(Task.now)
    case GET -> Root / "words" / IntVar(howMany) =>
      Ok(getTopWords(howMany))(CirceImplicits.jsonEncoder)
  }
}


object Main extends ServerApp {
  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(WordsController.wordCloudService, "/")
      .start
  }
}