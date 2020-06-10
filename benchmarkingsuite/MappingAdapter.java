package benchmarkingsuite;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MappingAdapter{

  // public JSONObject jmap; // JSONMapping object
  public HashMap<String,String> jmap;
  public int count = 0;

  // add a field-type pair to the HashMap
  public void addToMAP(String field_name,Object value){
    jmap.put(field_name,(String)value);
  }

  // function to parse JSON mapping file and add the field-type pairs in the HashMap
  public void parseJson(JSONObject jo,String fieldname_cur){
    JSONObject jo1 = (JSONObject)jo.get("properties");
    if(jo1!=null)
    {
      jo=jo1;
    }
    for(Iterator iterator = jo.keySet().iterator(); iterator.hasNext();) {

      String key = (String) iterator.next();

      if(!(jo.get(key) instanceof JSONObject)){
        if(jo.get(key) instanceof JSONArray){
          JSONArray jsonArray=(JSONArray)(jo.get(key));
          for (Object o:jsonArray){
            if(o instanceof JSONObject){
              if(fieldname_cur.length()>0)
                parseJson((JSONObject)o,fieldname_cur+"."+key);
              else
                parseJson((JSONObject)o,key);
            }
            else if(key.equals("type")){
                addToMAP(fieldname_cur,o);
            }
          }
        }
        else if(key.equals("type")){
            addToMAP(fieldname_cur,jo.get(key));
        }
      }
      else{
        if(fieldname_cur.length()>0)
          parseJson((JSONObject)jo.get(key),fieldname_cur+"."+key);
        else
          parseJson((JSONObject)jo.get(key),key);
      }

    }
  }

  // function to create a new HashMap by parsing the mapping file present at mapping_path location
  public void setMap(String mapping_path){
    JSONObject jo = new JSONObject();
    jmap = new HashMap<String,String>();
    try (FileReader reader = new FileReader(mapping_path)) {
      JSONParser par=new JSONParser();
      Object obj = par.parse(reader);
      jo=(JSONObject)obj;
      jo = (JSONObject)jo.get("mappings");					 // object of mapping holder of json file
      jo = (JSONObject)jo.get("message");				   	 // object of message holder of json file
      parseJson(jo,"");
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (ParseException e) {
        e.printStackTrace();
    }

  }

  // fucntion to return the type of field_name
  public String getType(String field_name){
    return jmap.get(field_name);
  }


}
