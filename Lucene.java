import java.io.*;
import java.util.*;
import java.nio.file.*;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.document.LongRange;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;


public class Lucene{
  private Lucene(){}
  public static void main(String[] args) throws Exception{
      Directory dir = FSDirectory.open(Paths.get("index"));
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      iwc.setOpenMode(OpenMode.CREATE);
      IndexWriter writer = new IndexWriter(dir, iwc);
      Document doc=new Document();

      Field sample1=new StringField("teststring","ritick",Field.Store.YES);
      doc.add(sample1);
      Field sample0=new TextField("mD.picture.hey","Lucene project for Sprinklr",Field.Store.YES);
      doc.add(sample0);

      Field sample2=new LongPoint("test_long",17116055);
      doc.add(sample2);
      for(int i=1;i<=1000;i++)
      {
        Field sample3=new LongPoint("test_long",i);
        doc.add(sample3);
      }
      System.out.println(doc.get("mD.picture.hey"));
      writer.addDocument(doc);
      writer.close();



      IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
      IndexSearcher searcher = new IndexSearcher(reader);
      QueryParser parser = new QueryParser("teststring", analyzer);

      String test_query0="+mD.picture.hey:Sprinklr";
      String test_query1="teststring:\"Ritick\"";
      String test_query2="test_long:1";
      Query query0=parser.parse(test_query0);
      Query query1=parser.parse(test_query1);
      // Query query2=parser.parse(test_query2);
      Query query3=new TermQuery(new Term("teststring","Ritick"));
      Query query2=LongPoint.newExactQuery("test_long",17116055);

      System.out.println(searcher.count(query1));

      TopDocs results0 = searcher.search(query0, 10);
      TopDocs results1 = searcher.search(query1, 10);
      TopDocs results2 = searcher.search(query2, 10);
      TopDocs results3 = searcher.search(query3, 10);

      int numTotalHits0 = Math.toIntExact(results0.totalHits.value);
      int numTotalHits1 = Math.toIntExact(results1.totalHits.value);
      int numTotalHits2 = Math.toIntExact(results2.totalHits.value);
      int numTotalHits3 = Math.toIntExact(results3.totalHits.value);

      System.out.println(numTotalHits0 + " total matching documents for "+ query0);
      System.out.println(numTotalHits1 + " total matching documents for "+ query1);
      System.out.println(numTotalHits2 + " total matching documents for "+ query2);
      System.out.println(numTotalHits3 + " total matching documents for "+ query3);

  }

}
