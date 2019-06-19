package project3;

import java.util.ArrayList;
import java.util.Scanner;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JDBCConnect con = new JDBCConnect();;
		con.connectDB();
		String schemaName = con.getSchemaName();
		
		int mode = 0;
		while(mode != 4) {
			System.out.print("Please input the instruction number (1: Import from CVS, 2: Export to CVS, 3: Manipulate Data, 4: Exit):");
			Scanner scn = new Scanner(System.in);
			mode = scn.nextInt();
			scn.nextLine();

			if(mode == 1) {
				ReadCSV rCSV = new ReadCSV(schemaName);
				String tableQ = rCSV.getMakeTableQuery();
				con.executeTableSQL(tableQ);
				ArrayList<String> tableDataQ = rCSV.getInsertDataQuery();
				if(tableDataQ!=null)
				{
					//execute insert table data
					con.executeInsertTableDB(tableDataQ, rCSV.getCSVRaw());
				}
			}
			if(mode == 2) {
				
			}
			
		}
	}
}
