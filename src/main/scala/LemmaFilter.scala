import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{TokenFilter, TokenStream}

class LemmaFilter(tokenStream:TokenStream) extends TokenFilter(tokenStream){
  val termAtt:CharTermAttribute = addAttribute(classOf[CharTermAttribute])

  override def incrementToken(): Boolean = {
    if (input.incrementToken()) {
      val termBuffer:Array[Char] = termAtt.buffer()
      val length: Int = termAtt.length()
      val term: String = termBuffer.mkString.substring(0, length)
      val lemma: String = Lemmatizer.plainTextToLemmas(term)
      val finalTerm:String = lemma
      val newLength:Int = lemma.length
      if (finalTerm != term)
        termAtt.copyBuffer(finalTerm.toCharArray, 0, newLength)
      else
        termAtt.setLength(newLength)
      true
    } else {
      false
    }
  }
}
