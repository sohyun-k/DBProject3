package project3;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class JDBCConnect {
	private static final String path = "./connection.txt";
	private static final String DB_DRIVER = "org.postgresql.Driver";
	private String DB_CONNECTION_URL = "jdbc:postgresql://";
	private String IP = "";
	private String DB_NAME = "";
	private String SCHEMA_NAME = "";
	private String ID = "";
	private String PW = "";
	
	private void parsingTXT() {
		// parsing txt file
		try {

			File file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
			
			String line = "";
			while((line=br.readLine())!=null) {
				line = line.replace(" ", "");
				String first = line.split(":")[0];
				String second = line.split(":")[1];
				
				if(first.equals("IP")) {
					IP = second;
				}
				else if(first.equals("DB_NAME")) {
					DB_NAME = second;
				}
				else if(first.equals("SCHEMA_NAME")) {
					SCHEMA_NAME = second;
				}
				else if(first.equals("ID")) {
					ID = second;
				}
				else if(first.equals("PW")) {
					PW = second;
				}	
			}
			br.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("connection.txt 파일 없음");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

		// set DB_DRIVER
		DB_CONNECTION_URL = DB_CONNECTION_URL+IP+"/"+DB_NAME;
		System.out.println(DB_CONNECTION_URL);
	}
	
	public void connectDB() {
		parsingTXT();
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Properties connProps = new Properties();
		
		// Setting Connection Info
		connProps.setProperty("user", ID);
		connProps.setProperty("password", PW);
		try {
			Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, connProps);
			Statement st = conn.createStatement();
		} catch (SQLException sqlEX) {
			System.out.println(sqlEX);
		}
		System.out.println("connection완료");
	}
}

