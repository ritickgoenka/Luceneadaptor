package benchmarkingsuite;



public class Test1{

  public static void main(String[] args) throws Exception  {

    String query_path = "queries";
    String message_path = "testdoc1";
    String mapping_path = "mapping.json";
    String url = "https://hooks.slack.com/services/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";

    Benchmark benchmark = new Benchmark();
    benchmark.registerQueries(query_path);
    System.out.println("registerd");
    benchmark.execute(5,message_path,mapping_path);
    System.out.println("added");

    Report report = benchmark.getReport();
    report.publishSlack(url);
    System.out.println("published");
  }

}
