package benchmarkingsuite;



public class Test1{

  public static void main(String[] args) throws Exception  {

    String query_path = "queries";  // location where queries text file is located
    String message_path = "testdoc1"; // Folder where message files are located
    String mapping_path = "mapping.json"; // location where mapping file is located
    String url = "https://hooks.slack.com/services/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"; //url of incoming webhook where the report is to be published
    int batch_size = 5;  // batch size of documents to match

    Benchmark benchmark = new Benchmark();

    //register queries
    benchmark.registerQueries(query_path);

    //match the documents against regitered queries
    benchmark.execute(batch_size,message_path,mapping_path);

    //Generate report and publish it on Slack channel
    Report report = benchmark.getReport();
    report.publishSlack(url);
  }

}
