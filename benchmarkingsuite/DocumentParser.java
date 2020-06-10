package benchmarkingsuite;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Document;

public class DocumentParser{

  public MappingAdapter mapping_adapter = new MappingAdapter();

  public DocumentParser(String mapping_path)
  {
    this.mapping_adapter.setMap(mapping_path);
  }

  //update new MappingAdapter, Json file is located at "mapping_path"
  public void setMappingAdapter(String mapping_path)
  {
    this.mapping_adapter.setMap(mapping_path);
  }

  //function to add a field to the document
  public void addToDoc(String field,Object value,Document doc)throws Exception{
      String type=mapping_adapter.getType(field);
      if(type == null||value == null){
        return;
      }
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
  public void addJsonDocument(JSONObject jo,String fieldname_cur,Document doc)throws Exception{

    for(Iterator iterator = jo.keySet().iterator(); iterator.hasNext();) {

      String key = (String) iterator.next();

      if(!(jo.get(key) instanceof JSONObject)){
        if(jo.get(key) instanceof JSONArray){
          JSONArray jsonArray=(JSONArray)(jo.get(key));
          for (Object o:jsonArray){
            if(o instanceof JSONObject){
              if(fieldname_cur.length()>0)
                addJsonDocument((JSONObject)o,fieldname_cur+"."+key,doc);
              else
                addJsonDocument((JSONObject)o,key,doc);
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
        addJsonDocument((JSONObject)jo.get(key),fieldname_cur+"."+key,doc);
      else
        addJsonDocument((JSONObject)jo.get(key),key,doc);
    }
  }

  // function to add field-value pairs present in a JSON file at location path
  public void addJsonDocument(String path, List<Document> docs)throws Exception
  {
    JSONObject jo=new JSONObject();
    try (FileReader reader = new FileReader(path)) {
      BufferedReader bufferedReader = new BufferedReader(reader);
      Object obj ;
      String line;
      while((line = bufferedReader.readLine()) != null) {
          Document doc = new Document();
          obj = (JSONObject) new JSONParser().parse(line);
          jo=(JSONObject)obj;
          addJsonDocument(jo,"",doc);
          docs.add(doc);
      }

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }
    int i=1;


    // return doc;
  }

}
