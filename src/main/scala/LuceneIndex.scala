import java.io.Reader

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{LowerCaseFilter, StopFilter}
import org.apache.lucene.analysis.es.SpanishAnalyzer
import org.apache.lucene.analysis.miscellaneous.{ASCIIFoldingFilter, PerFieldAnalyzerWrapper}
import org.apache.lucene.analysis.standard.{StandardAnalyzer, StandardFilter, StandardTokenizer}
import org.apache.lucene.analysis.util.StopwordAnalyzerBase
import org.apache.lucene.analysis.{Analyzer, TokenStream, Tokenizer}
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.{Document, TextField}
import org.apache.lucene.index
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.misc.HighFreqTerms
import org.apache.lucene.queries.TermsQuery
import org.apache.lucene.queries.mlt.MoreLikeThis
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.{Directory, RAMDirectory}

import scala.collection.JavaConversions._
import scala.io.Source

object LuceneIndex {
  val directory: Directory = new RAMDirectory()
  val analyzer: StandardAnalyzer = new StandardAnalyzer(SpanishAnalyzer.getDefaultStopSet)

  def createIndex(): Unit = {
    val analyzers: Map[String, Analyzer] = Map("similar" -> StemAnalyzer, "original" -> analyzer)
    val wrapper: PerFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(analyzer, analyzers)

    val indexWriter: IndexWriter = new index.IndexWriter(directory,
      new IndexWriterConfig(wrapper).setOpenMode(OpenMode.CREATE))
    val document: Document = new Document()
    try {
      for (line <- Source.fromInputStream(getClass.getResourceAsStream("text.txt")).getLines()) {
//        document.add(new TextField("similar", line, Store.NO))
        document.add(new TextField("original", line, Store.NO))
        indexWriter.addDocument(document)
      }
    } finally {
      indexWriter.close()
    }
  }

  def topTerms(howMany: Int): Map[String, Long] = {
    val reader: DirectoryReader = DirectoryReader.open(directory)
    HighFreqTerms.getHighFreqTerms(reader, howMany, "original", new HighFreqTerms.TotalTermFreqComparator())
      .map { term =>
        printf("Term: %s, Frequency: %d \n", term.termtext.utf8ToString(), term.totalTermFreq)
        (term.termtext.utf8ToString(), term.totalTermFreq)
      }.toMap
  }

  def addSimilarTerms(terms: List[String]): List[String] = {
    val results = List.newBuilder[String]

    // Open the index, false flag indicates it's writable,
    // since we need to delete similar docs
    val indexReader: DirectoryReader = DirectoryReader.open(directory)

    val searcher = new IndexSearcher(indexReader)
    val allTerms: List[Term] = terms.map(term => new Term("similar", Lemmatizer.plainTextToLemmas(term)))
    val termsQuery: TermsQuery = new TermsQuery(allTerms)
    val topDocs = searcher.search(termsQuery, terms.size)
    try {

      // Initialize MoreLikeThis with indexReader
      val moreLikeThis = new MoreLikeThis(indexReader)
      moreLikeThis.setFieldNames(Array("original"))
      // Lower the word and doc frequency since message content is short
      moreLikeThis.setMinTermFreq(1)
      moreLikeThis.setMinDocFreq(1)

      // Iterate all docs, find similar ones and remove
      for (docNum <- topDocs.scoreDocs) {

        // MoreLikeThis will return a query for searching,
        // which is the important words I mentioned before.
        val query = moreLikeThis.like(docNum.doc)

        // Then use normal search functionality, with the scoreThreshold,
        // to find all the similar docs and remove.
        val searcher = new IndexSearcher(indexReader)
        val topDocs = searcher.search(query, 100)
        for (scoreDoc <- topDocs.scoreDocs) {
          if (scoreDoc.score > 0.5f) {
            // Delete all similar docs
            //            doc.clear()
          }
        }
      }
      List.concat(terms, results.result())
    } finally {
      indexReader.close()
    }
  }
}


object StemAnalyzer extends StopwordAnalyzerBase {
  val source: Tokenizer = new StandardTokenizer
  var result: TokenStream = new StandardFilter(source)
  result = new LowerCaseFilter(result)
  result = new StopFilter(result, SpanishAnalyzer.getDefaultStopSet)
  result = new ASCIIFoldingFilter(result)
  result = new LemmaFilter(result)

  override def createComponents(fieldName: String): TokenStreamComponents = new Analyzer.TokenStreamComponents(source, result) {
    override protected def setReader(reader: Reader) {
      super.setReader(reader)
    }
  }
}