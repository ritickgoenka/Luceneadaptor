package benchmarkingsuite;

import java.io.IOException;
import java.lang.ProcessBuilder;
import java.lang.Process;

public class Report{
  public final long message_count;
  public final long query_count;
  public final long query_match_count;
  public final long query_match_time;

  public Report(long message_count,long query_count,long query_match_count,long query_match_time){
    this.message_count = message_count;
    this.query_count = query_count;
    this.query_match_count = query_match_count;
    this.query_match_time = query_match_time;
  }

  public void publishSlack(String url){
    String data = "Message Count = "+this.message_count+"  |  Query Count = "+this.query_count+"  |  Query Match Count = "+this.query_match_count+"  |  Query Match Time (in ms) = "+this.query_match_time;
    data = "{\"text\":\""+data+"\"}";
    String[] command = new String[] { "/bin/bash", "-c", "curl -X POST -H 'Content-type: application/json' --data '"+data+"' "+url};
    try {
      Process proc = new ProcessBuilder(command).start();
    } catch (IOException e) {
       e.printStackTrace();
    }
  }

}
