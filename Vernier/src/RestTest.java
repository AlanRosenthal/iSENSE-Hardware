import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import REST.RestAPI;
import REST.columnMatchException;
import REST.isenseInterface;
import REST.loginException;

public class RestTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JSONObject get_status_json = new JSONObject(GetStatus());
			int col_size = get_status_json.getJSONObject("views").length();
			long start_time = Long.parseLong(get_status_json.getString("columnListTimeStamp"))*1000;//time started, in unix milliseconds
			String col_id[] = new String[col_size];//column id
			String col_type[] = new String[col_size];//type of date eg time, temp, ph
			String col_data[] = new String[col_size];//column data
			//gets data from vernier labquest 2, in JSON format, puts into strings
			for (int i = 0;i<col_size;i++){//loop through all the columns available
				String views = get_status_json.getJSONObject("views").names().getString(i);
				if (get_status_json.getJSONObject("views").getJSONObject(views).has("baseColID")){
					col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("baseColID");
				} else if (get_status_json.getJSONObject("views").getJSONObject(views).has("colID")) {
					col_id[i] = get_status_json.getJSONObject("views").getJSONObject(views).getString("colID");
				}
				col_type[i] = get_status_json.getJSONObject("columns").getJSONObject(col_id[i]).getString("name");
				JSONObject get_col_json = new JSONObject(GetColumns(col_id[i]));
				col_data[i] = get_col_json.getString("values");				
			}
			//gets data from strings and puts time data in time, and each data set in data, and type in type
			ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
			ArrayList<String> type = new ArrayList<String>();
			for (int i = 0;i<col_size;i++){
				ArrayList<String> temp = new ArrayList<String>();
				if ((col_type[i].compareTo("Time") == 0) && (!type.contains("Time"))){ //time
					StringTokenizer time_tokenized = new StringTokenizer(col_data[i],"[,]");
					while (time_tokenized.hasMoreTokens()){
						temp.add(Long.toString((long)Double.parseDouble(time_tokenized.nextToken())*1000+start_time));
					}
					data.add(temp);
					type.add(col_type[i]);
				}
				else if (col_type[i].compareTo("Time") != 0){
					StringTokenizer data_tokenized = new StringTokenizer(col_data[i],"[,]");
					while (data_tokenized.hasMoreTokens()){
						temp.add(data_tokenized.nextToken());
					}
					data.add(temp);
					type.add(col_type[i]);
				}
			}

			System.out.println(data);			
			isenseInterface is = new isenseInterface();
			int exp = 485;
			String headers[] = type.toArray(new String[type.size()]);

			Vector<String> test_data = new Vector<String>();
			test_data.add("1,2,3");
			System.out.println(test_data);
			Vector<String> vector_data = new Vector<String>();
			for (int i = 0;i<data.get(0).size();i++){
				String temp = new String();
				for (int j = 0;j<data.size();j++){
					if (j != 0){
						temp = temp + ",";
					}
					temp = temp + data.get(j).get(i);
				}
				vector_data.add(temp);
			}
			System.out.println(vector_data);
			System.out.println(type);
			
			is.login("test", "test");
			int sid = is.joinExperiment(exp, "Testing LapQuest", "Procedure....", "123 Fake St.", "Springfield, USA");
			System.out.println("Created Session " + sid );
			//is.addToSession(exp, sid, headers , data2);
			is.addToSession(exp, sid, headers , vector_data);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (loginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (columnMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public static Vector<String> getData(){
		Vector<String> data = new Vector<String>();
		
		data.add("time,ph,temp,temp");
		
		return data;
	}
	
	public static String StartRecording(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/control/start");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String StopRecording(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/control/stop");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String GetInfo(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/info");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String GetStatus(){
		String result = null;
		try {
			result = httpGet("http://labquest.local/status");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String GetColumns(String column){
		String result = null;
		try {
			result = httpGet("http://labquest.local/columns/" + column);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String httpGet(String urlStr) throws IOException {
		  URL url = new URL(urlStr);
		  HttpURLConnection conn =
		      (HttpURLConnection) url.openConnection();

		  if (conn.getResponseCode() != 200) {
		    throw new IOException(conn.getResponseMessage());
		  }

		  // Buffer the result into a string
		  BufferedReader rd = new BufferedReader(
		      new InputStreamReader(conn.getInputStream()));
		  StringBuilder sb = new StringBuilder();
		  String line;
		  while ((line = rd.readLine()) != null) {
		    sb.append(line);
		  }
		  rd.close();
		  conn.disconnect();
		  return sb.toString();
	  }
}