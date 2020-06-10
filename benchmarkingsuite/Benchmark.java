package benchmarkingsuite;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.io.File;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.document.Document;



import org.apache.lucene.search.Query;

import org.apache.lucene.monitor.*;

public class Benchmark{
  public Monitor monitor;
  public Analyzer analyzer;
  public SimpleQueryParser queryparser;
  public long doc_count = 0;
  public long match_count = 0;
  public long search_time = 0;


  // create a new Benchmark object with StandardAnalyzer
  public Benchmark()throws IOException{
    this(new StandardAnalyzer());
  }

  // create a new Benchmark object with Analyzer in the parameter
  public Benchmark( Analyzer analyzer) throws IOException{
    this.analyzer = analyzer;
    this.monitor = new Monitor(this.analyzer);
    this.queryparser = new SimpleQueryParser(this.analyzer,"text");
  }

  // create a new Report of the matches done till now
  public Report getReport()throws IOException{
    return new Report(doc_count,monitor.getQueryCount(),match_count ,search_time);
  }

  // regiter queries located at queryPath
  public void registerQueries(String queryPath)throws Exception{
    List<MonitorQuery> queries = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(queryPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,StandardCharsets.UTF_8))) {
        String queryId;
        String queryString;
        while ((queryId = br.readLine()) != null) {
            queryString = br.readLine();
            Query query = queryparser.parse(queryString);
            queries.add(new MonitorQuery(queryId, query));
        }
    }
    this.registerQueries(queries);
  }

  // register all the queries present in list "queries"
  public void registerQueries(List<MonitorQuery> queries)throws IOException{
    monitor.register(queries);
  }

  // match the documents present in the message_path folder in batches
  public void execute(int batch_size,String message_path, String mapping_path)throws Exception{
    File directoryPath = new File(message_path);
    String contents[] = directoryPath.list();
    List<Document> doc = new ArrayList<>();
    DocumentParser dp = new DocumentParser(mapping_path);
    for(int i=0; i<contents.length; i++) {
       dp.addJsonDocument(message_path+"/"+contents[i],doc);
       if((i+1)%batch_size == 0||i == contents.length-1){
         Document docs[] = new Document[doc.size()];
         docs = doc.toArray(docs);
         doc.clear();
         this.execute(docs);
       }
    }
  }

  // match all the docments present in docs array
  public void execute(Document[] docs) throws IOException{
    MultiMatchingQueries<QueryMatch> matches = monitor.match(docs,QueryMatch.SIMPLE_MATCHER);
    doc_count += matches.getBatchSize();
    for(int i=0;i<matches.getBatchSize();i++){
      match_count+=matches.getMatchCount(i);
    }
    search_time += matches.getSearchTime();
  }



}
