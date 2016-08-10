import org.annolab.tt4j.{TokenHandler, TreeTaggerWrapper}

object Lemmatizer {
  val treeTaggerHome = "/home/arielo/IdeaProjects/PaloTextAnalyzer/src/main/resources/treeTagger"
  System.setProperty("treetagger.home", treeTaggerHome)
  private val taggerWrapper: TreeTaggerWrapper[String] = new TreeTaggerWrapper[String]
  taggerWrapper.setModel(treeTaggerHome + "/models/spanish-utf8.par:utf-8")

  def plainTextToLemmas(text: String): String = {
    var extractedLemma = ""
    taggerWrapper.setHandler(new TokenHandler[String] {
      override def token(token: String, pos: String, lemma: String): Unit = {
        extractedLemma = lemma
      }
    })
    taggerWrapper.process(Array(text))
    extractedLemma
  }
}
