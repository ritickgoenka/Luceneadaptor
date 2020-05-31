import java.io.*;
import java.util.*;
import java.nio.file.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.monitor.*;


public class MonitorADP {
  public static JSONObject jmap; // JSONMapping object


  // function to get inside holder with key "holder_name"
  public static JSONObject getnext(JSONObject obj, String holder_name){
		return (JSONObject)obj.get(holder_name);
	}


  // function to get field type from JSON mapping eg: input:"snT" output:"keyword" (quotes for clarity)
  public static String findKey(String fieldname,JSONObject jo){
		String type="";								// go inside properties holder
		int len=fieldname.length();
		int last_index=0;
    // loop to go inside the second last possible holder where type key is present
		for(int i=0;i<len;i++){
			if(fieldname.charAt(i)=='.'){
				jo=getnext(jo,"properties");
				jo=getnext(jo,fieldname.substring(last_index,i));
				last_index=i+1;
			}
		}
    // go inside the last possible holder where type value is present
		jo=getnext(jo,"properties");
		jo=getnext(jo,fieldname.substring(last_index,len));
		type=(String)jo.get("type");

		return type;
	}


  //function to add a field to the document
  public static void addToDoc(String field,Object value,Document doc)throws Exception{
      String type=findKey(field,jmap);
      if(type.equals("text")){
        doc.add(new TextField(field,(String)value,Field.Store.YES));
      }
      else if(type.equals("keyword")){
        doc.add(new StringField(field,(String)value,Field.Store.YES));
      }
      else if(type.equals("long")){
        doc.add(new LongPoint(field,(long)value));
      }
      else if(type.equals("int")){
        doc.add(new IntPoint(field,(int)value));
      }
      else if(type.equals("boolean")){
        boolean val=(boolean)(value);
        doc.add(new StringField(field,(val)?"true":"false",Field.Store.YES));
      }
  }


  // function to add a all field value pairs present in JSONObject "jo" to Document "doc" using recursion
  // jo is the current JSONObject, fieldname_cur is the fieldname till current JSONObject
  public static void addJSON(JSONObject jo,String fieldname_cur,Document doc)throws Exception{

    for(Iterator iterator = jo.keySet().iterator(); iterator.hasNext();) {

      String key = (String) iterator.next();

      if(!(jo.get(key) instanceof JSONObject)){
        if(jo.get(key) instanceof JSONArray){
          JSONArray jsonArray=(JSONArray)(jo.get(key));
          for (Object o:jsonArray){
            if(o instanceof JSONObject){
              if(fieldname_cur.length()>0)
                addJSON((JSONObject)o,fieldname_cur+"."+key,doc);
              else
                addJSON((JSONObject)o,key,doc);
            }
            else
            {
              if(fieldname_cur.length()>0)
                addToDoc(fieldname_cur+'.'+key,o,doc);
              else
                addToDoc(key,o,doc);
            }
          }
          continue;
        }
        else{
          if(fieldname_cur.length()>0)
            addToDoc(fieldname_cur+'.'+key,jo.get(key),doc);
          else
            addToDoc(key,jo.get(key),doc);
          continue;
        }
      }
      if(fieldname_cur.length()>0)
        addJSON((JSONObject)jo.get(key),fieldname_cur+"."+key,doc);
      else
        addJSON((JSONObject)jo.get(key),key,doc);
    }
  }

  // function to add field-value pairs present in a JSON file at location path
  public static Document addJSON(String path)throws Exception
  {
    JSONObject jo=new JSONObject();
		try (FileReader reader = new FileReader(path)) {
			JSONParser par=new JSONParser();
			Object obj = par.parse(reader);
			jo=(JSONObject)obj;
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (ParseException e) {
		    e.printStackTrace();
		}
    int i=1;
    Document doc=new Document();
    addJSON(jo,"",doc);
    return doc;
  }

  public static void main(String[] args) throws Exception  {
        // Create analyzer(default), IndexWriterConfig(default) and IndexWriter(Default)

        Analyzer analyzer = new StandardAnalyzer();
        try(Monitor monitor=new Monitor(analyzer)){
          List<MonitorQuery> queries = new ArrayList<>();
          String test_query0 = "+mD.photoText:tokenized";
          String test_query1 = "+snT:Twitter";
          int count = 0;
          QueryParser parser = new QueryParser("text",analyzer);
          Query query0=parser.parse(test_query0);
          Query query1=LongPoint.newExactQuery("mTp",52);
          queries.add(new MonitorQuery(String.format( "%d", count++)+"-"+query0,query0));
          queries.add(new MonitorQuery(String.format( "%d", count++)+"-"+query1,query1));
          monitor.register(queries);

          String message_path="/home/ritick/message.json";
          String mapping_path="/home/ritick/mapping.json";
          jmap=new JSONObject();
          try (FileReader reader = new FileReader(mapping_path)) {
      			JSONParser par=new JSONParser();
      			Object obj = par.parse(reader);
      			jmap=(JSONObject)obj;
      		} catch (FileNotFoundException e) {
      		    e.printStackTrace();
      		} catch (IOException e) {
      		    e.printStackTrace();
      		} catch (ParseException e) {
      		    e.printStackTrace();
      		}
          jmap=getnext(jmap,"mappings");					// create mappings object
      		jmap=getnext(jmap,"message");
          Document doc=addJSON(message_path);
          MatchingQueries<HighlightsMatch> matches = monitor.match(doc,HighlightsMatch.MATCHER);
          Collection<HighlightsMatch> col=matches.getMatches();
          System.out.println("Total matches = "+matches.getMatchCount());
          if(matches.getMatchCount()>0)
          {
            System.out.println("Queries that matched were:");
          }
          for (HighlightsMatch docMatches : col) {
            System.out.println(docMatches.getQueryId());
          }
        }
  }

}
