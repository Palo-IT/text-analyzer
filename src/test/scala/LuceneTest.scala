import org.scalatest.{FlatSpec, GivenWhenThen}

class LuceneTest extends FlatSpec with GivenWhenThen {
  it should "Return the requested top X words" in {
    Given("A text file")
    When("We create a Lucene Index in Memory")
    LuceneIndex.createIndex()
    val topTerms: Map[String, Long] = LuceneIndex.topTerms(30)

    Then("We have an In Memory Index!")
    assert(topTerms.size == 30)

    When("We create a 'Like' search")
    val similarTerms: List[String] = LuceneIndex.addSimilarTerms(topTerms.keys.toList)
    Then("We have some 'likes'")
    assert(similarTerms.size >= topTerms.size)
  }
}
