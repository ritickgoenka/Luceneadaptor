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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

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


public class Luceneadp {
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
        Directory dir = FSDirectory.open(Paths.get("index"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        // parse mapping file and call addJSON(String path) function to get a document
        // with field-value pairs present in JSON file located at the given path
        // add the document to IndexWriter and doc will be added to index created in a folder
        // named "index" at the location where this Luceneadp.java file is present
        // to change the location of folder edit dir object above
        // same location must be used by Index Reader
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
        // System.out.println("doc")
        writer.addDocument(doc);
        writer.close();

        // create IndexSearcher and QueryParser objects
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser parser = new QueryParser("text", analyzer);

        // hard coded query testing
        // I'm facing problem in testing test_query1 which has type="keyword", I have added it as StringField
        String test_query0="+mD.photoText:tokenized";

        Query query0=parser.parse(test_query0);
        Query query1=new TermQuery(new Term("snT","Twitter"));
        Query query2=new TermQuery(new Term("iBP","false"));
        Query query3=LongPoint.newExactQuery("mTp",52);


        TopDocs results0 = searcher.search(query0, 10);
        TopDocs results1 = searcher.search(query1, 10);
        TopDocs results2 = searcher.search(query2, 10);
        TopDocs results3 = searcher.search(query3, 10);

        int numTotalHits0 = Math.toIntExact(results0.totalHits.value);
        int numTotalHits1 = Math.toIntExact(results1.totalHits.value);
        int numTotalHits2 = Math.toIntExact(results2.totalHits.value);
        int numTotalHits3 = Math.toIntExact(results3.totalHits.value);

        System.out.println(numTotalHits0 + " total matching documents for "+query0);
        System.out.println(numTotalHits1 + " total matching documents for "+query1);
        System.out.println(numTotalHits2 + " total matching documents for "+query2);
        System.out.println(numTotalHits3 + " total matching documents for "+query3);
  }

}
