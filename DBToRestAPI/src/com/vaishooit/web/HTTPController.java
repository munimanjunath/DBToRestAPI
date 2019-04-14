package com.vaishooit.web;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaishooit.core.ResultSetSerializer;

//Extend HttpServlet class
public class HTTPController extends HttpServlet {

	HashMap<String,HashMap<String,HashMap<String,String>> >  tableMap = new HashMap<String,HashMap<String,HashMap<String,String>>>();
	DataSource ds = null;
	  HashMap<String,String> queryMap = new HashMap<String,String>() ;


public void init() {
	
	try {
		

	InitialContext cxt = new InitialContext();
	if ( cxt == null ) {
	   throw new Exception("Uh oh -- no context!");
	}

	 ds = (DataSource) cxt.lookup( "java:/comp/env/RestDB" );
	 Connection  conn =  ds.getConnection() ;
     Statement stmt = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
 		    ResultSet.CONCUR_READ_ONLY);
	   ResultSet rs;
	   String dbQueryString = "SELECT queryname,query  FROM api_queries" ; 
	 
   
    System.out.println(dbQueryString);

    rs = stmt.executeQuery(dbQueryString);
    
  while ( rs.next() ) {
	  
	  String queryName = rs.getString(1);
	  String query  = rs.getString(2);
	  
	  System.out.println(queryName+":    "+query );
	  
      queryMap.put(queryName, query)	   ;
  
}
  
  rs.close();
  
  HashMap<String,String> oraTableMap = new HashMap<String,String>() ;

rs = stmt.executeQuery("Select * from tab"  );
  
while ( rs.next() ) {
	  
	  String queryName = rs.getString(1);
	  String query  = rs.getString(2);
	  
	  System.out.println(queryName+":    "+query );
	  
	  oraTableMap.put(queryName, query)	   ;

}

rs.close();
  
  
  
	DatabaseMetaData databaseMetaData = conn.getMetaData();
	//Print TABLE_TYPE "TABLE"
	ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE","VIEW"});
	System.out.println("Printing TABLE_TYPE \"TABLE\" ");
	System.out.println("----------------------------------");
	while(resultSet.next())
	{
	    //Print
		
		if(!oraTableMap.containsKey(resultSet.getString("TABLE_NAME"))) {
			continue ;
		}
	    System.out.println("TableName: "+ resultSet.getString("TABLE_NAME") +"  "+ resultSet.getString("TABLE_TYPE"));
      ResultSet columnRS = databaseMetaData.getColumns(conn.getCatalog(), null, resultSet.getString("TABLE_NAME"), "%");
      HashMap<String,HashMap<String,String>> columnMap = new HashMap<String,HashMap<String,String>>() ;
      
      while(columnRS.next()) {
	        HashMap<String,String> columnDescMap = new HashMap<String,String>() ;

		    System.out.println( columnRS.getString("COLUMN_NAME")+"   typename:"+ columnRS.getString("TYPE_NAME")+" "+columnRS.getString("NULLABLE") );
		    columnDescMap.put( "TYPE_NAME",  columnRS.getString("TYPE_NAME"));
		    columnDescMap.put( "NULLABLE",  columnRS.getString("NULLABLE"));
		    columnDescMap.put( "COLUMN_NAME",  columnRS.getString("COLUMN_NAME"));
		    columnMap.put(columnRS.getString("COLUMN_NAME"), columnDescMap);

      	
      }
      columnRS.close();
      
      tableMap.put(resultSet.getString("TABLE_NAME") , columnMap ) ;
	}
	resultSet.close();
  
  rs.close() ;
  conn.close();
    
  
		
		conn.close();
	
	
	}catch(Exception e ) {
		
		e.printStackTrace();
		
	}
	
	
	
}
  
   
// Method to handle GET method request.
   public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      // Set response content type
      response.setContentType("application/json");

      PrintWriter out = response.getWriter();
    

//      out.println("{'name' : 'Hello World'}"
//      );
      
      
      SimpleModule module = new SimpleModule();
      module.addSerializer(new ResultSetSerializer());

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(module);

      
      System.out.println(request.getContextPath());
      System.out.println(request.getPathInfo());
      
      String pathInfo = request.getPathInfo() ;
      String[] pathArr = pathInfo.split("/");
      System.out.println("0:"+pathArr[0]+"   1:"+pathArr[1]+" 2:"+pathArr[2]) ;
      for(String c : pathArr) {
    	  System.out.println(c);
      }
      
       Map<String,String[]> parameterMap = request.getParameterMap();
      String condition = parameterMap.get("condition")!=null && parameterMap.get("condition").length > 0 ? parameterMap.get("condition")[0] : null ;
      
      if(condition != null && condition.length() > 1 ) {
    	  condition = condition.replace("$eq", "=");
    	  
 	  }
      
      
      
    	  
     
      String tableName =  pathArr[2] ;
      
      try {
          Connection conn = ds.getConnection() ;
          Statement stmt = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
        		    ResultSet.CONCUR_READ_ONLY);
          ResultSet rs;
          
          
          String dbQueryString = null ;
          
          if(pathArr[1].equals("table") ) {
        	  
          
	          dbQueryString = "SELECT *  FROM "+tableName ; 
	          
	          if( condition!= null )
	        	  dbQueryString += " where "+ condition ;
	          
	          System.out.println(dbQueryString);
          
          
          
          }else  if(pathArr[1].equals("query") )  {
        	  
        	  dbQueryString =  queryMap.get(pathArr[2]);
	          System.out.println(dbQueryString);
	          
	          Map<String,String[]> queryParametersMap = getQueryParameters(request) ;
	          
	          for(String c : queryParametersMap.keySet() ) {
	        	  
	        	  System.out.println(c+"    "+queryParametersMap.get(c)[0]);
	        	  
	        	  String  dbParameter = "{{"+c+"}}" ;
	        	  
	        	  dbQueryString = dbQueryString.replace(dbParameter, queryParametersMap.get(c)[0]);
	          }

        	  
          }else {
        	  
        	  response.setStatus(500,"Invalid Select Type");
    		  out.print("{\"ErrorMessage\" : \"Invalid Select Type. Select type should be table/query \"}");
              out.flush();
        	  
        	  
          }

          rs = stmt.executeQuery(dbQueryString);
          
          ObjectNode objectNode = objectMapper.createObjectNode();
          objectNode.putPOJO("results", rs);
          
          StringWriter stringWriter = new StringWriter();

          objectMapper.writeValue(stringWriter, objectNode);
          
          String jsonInString = stringWriter.toString();
        //  System.out.println(jsonInString);
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          out.print(jsonInString);
          out.flush();




          rs.close();
          conn.close();
      } catch (Exception e) {
    	  e.printStackTrace();
          System.err.println("Got an exception! ");
          System.err.println(e.getMessage());
      }
      
      
      
     // System.out.println(request.get)

      
   }
   
   // Method to handle POST method request.
   public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
   }
   
   
   public static Map<String, String[]> getQueryParameters(HttpServletRequest request) {
	    Map<String, String[]> queryParameters = new HashMap<>();
	    String queryString = request.getQueryString();

	    if (StringUtils.isEmpty(queryString)) {
	        return queryParameters;
	    }

	    String[] parameters = queryString.split("&");

	    for (String parameter : parameters) {
	        String[] keyValuePair = parameter.split("=");
	        String[] values = queryParameters.get(keyValuePair[0]);
	        values = ArrayUtils.add(values, keyValuePair.length == 1 ? "" : keyValuePair[1]); //length is one if no value is available.
	        queryParameters.put(keyValuePair[0], values);
	    }
	    return queryParameters;
	}

}
