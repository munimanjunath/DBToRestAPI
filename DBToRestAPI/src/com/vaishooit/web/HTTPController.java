package com.vaishooit.web;


import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

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
	   String dbQueryString = "SELECT *  FROM api_queries" ; 
	 
   
    System.out.println(dbQueryString);

    rs = stmt.executeQuery(dbQueryString);
    
  while ( rs.next() ) {
	  
	  String queryAPI = rs.getString("");
	  String query  = rs.getString("");
	  
      queryMap.put(queryAPI, query)	   ;
  
}
  
  rs.close() ;
  conn.close();
    
    
	//	DatabaseMetaData databaseMetaData = conn.getMetaData();
		//Print TABLE_TYPE "TABLE"
//		ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE","VIEW"});
//		System.out.println("Printing TABLE_TYPE \"TABLE\" ");
//		System.out.println("----------------------------------");
//		while(resultSet.next())
//		{
//		    //Print
//		    System.out.println("TableName: "+ resultSet.getString("TABLE_NAME") +"  "+ resultSet.getString("TABLE_TYPE"));
//	        ResultSet columnRS = databaseMetaData.getColumns(conn.getCatalog(), null, resultSet.getString("TABLE_NAME"), "%");
//	        HashMap<String,HashMap<String,String>> columnMap = new HashMap<String,HashMap<String,String>>() ;
//	        
////	        while(columnRS.next()) {
////		        HashMap<String,String> columnDescMap = new HashMap<String,String>() ;
////
////			    System.out.println( columnRS.getString("COLUMN_NAME")+"   typename:"+ columnRS.getString("TYPE_NAME")+" "+columnRS.getString("NULLABLE") );
////			    columnDescMap.put( "TYPE_NAME",  columnRS.getString("TYPE_NAME"));
////			    columnDescMap.put( "NULLABLE",  columnRS.getString("NULLABLE"));
////			    columnDescMap.put( "COLUMN_NAME",  columnRS.getString("COLUMN_NAME"));
////			    columnMap.put(columnRS.getString("COLUMN_NAME"), columnDescMap);
////
////	        	
////	        }
//	        columnRS.close();
//	        
//	        tableMap.put(resultSet.getString("TABLE_NAME") , columnMap ) ;
//		}
//		resultSet.close();
		
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
      System.out.println("0:"+pathArr[0]+"   1:"+pathArr[1]) ;
      for(String c : pathArr) {
    	  System.out.println(c);
      }
      
       Map<String,String[]> parameterMap = request.getParameterMap();
      String condition = parameterMap.get("condition")!=null && parameterMap.get("condition").length > 0 ? parameterMap.get("condition")[0] : null ;
      
      
      String tableName = pathArr[1] ;
      
      
      try {
          Connection conn = ds.getConnection() ;
          Statement stmt = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
        		    ResultSet.CONCUR_READ_ONLY);
          ResultSet rs;
          String dbQueryString = "SELECT *  FROM "+tableName ; 
          
          if( condition!= null )
        	  dbQueryString += " where "+ condition ;
          
          System.out.println(dbQueryString);

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


//          
//          HashMap<String,HashMap<String,String>> columnMap = tableMap.get(tableName);
//          Iterator<String> columnIter = columnMap.keySet().iterator();
//          String[] columnArr = new String[columnMap.keySet().size()];
//          int  i=0 ;
//          while(columnIter.hasNext() ) {
// 	    	 
//      	    String columnName = columnIter.next() ;
//      	   columnArr[i++] = columnName ;
//      	   
//      	    }
//          
//          int rowcount = 0;
//          if (rs.last()) {
//            rowcount = rs.getRow();
//            rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
//          }
//          
//          Object[] rowsArr  = new Object[rowcount];
//          i=0;
//          while ( rs.next() ) {
//        	  
//        	  
//        	  
//        	  HashMap<String,Object> columnDataMap = new HashMap<String,Object>() ;
//        	  
//        	  for(String columnName  : columnArr) {
//        		  String columnValue = rs.getString(columnName);
//                  System.out.println("columnName:"+columnName+"   columnValue:"+columnValue);
//                  columnDataMap.put(columnName, columnValue);
//                  
//        	  }
//        	  rowsArr[i++]= columnDataMap ;
//        	   
//            
//          }
//          
//          ObjectMapper mapper = new ObjectMapper();
//          String jsonInString = mapper.writeValueAsString(rowsArr);
//          System.out.println(jsonInString);
//          response.setContentType("application/json");
//          response.setCharacterEncoding("UTF-8");
//          out.print(jsonInString);
//          out.flush();   

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
      
      doGet(request, response);
   }

}