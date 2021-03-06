import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MovieDriver {

	public static boolean processMoviePeopleSong(){
		Connection allConn = null;
		try{ 	
			allConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/omdb", "root", "");
			
			Statement allStmt = allConn.createStatement();
                        
			ResultSet rsmps = allStmt.executeQuery("SELECT * FROM mpr_test_data");
			PreparedStatement pmaxid = allConn.prepareStatement("SELECT max(id) FROM mpr_test_data", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet maxIDrsmps = pmaxid.executeQuery();
			int maxID = 0;
			if( maxIDrsmps.next()){
				maxID = maxIDrsmps.getInt(1);
			}

			
			for (int i = 0; i < maxID; i++){
				
				rsmps.next();
				int moviecreated = 0;
				int peoplecreated = 0;
				int mpcreated = 0;

				int currentMovie = 0;

				//Check and Create Movies
				String nativeNameTD = rsmps.getString("native_name");
				int yearMadeTD = rsmps.getInt("year_made");
			
				PreparedStatement ps = allConn.prepareStatement("SELECT * FROM movies WHERE native_name = ? AND year_made = ?");
				ps.setString(1, nativeNameTD);
				ps.setInt(2,yearMadeTD);

				ResultSet mrs = ps.executeQuery();
				//mrs.beforeFirst();
				if (mrs.next() == true)
				{
					System.out.println("Movie already exists.");
					currentMovie += 1;
				}
				else
				{
					PreparedStatement moviePS = allConn.prepareStatement("INSERT INTO movies (native_name, year_made, english_name) VALUES (?,?,?)");
					moviePS.setString(1,nativeNameTD);
					moviePS.setInt(2,yearMadeTD);
					moviePS.setString(3,nativeNameTD);

					int row = moviePS.executeUpdate();
					System.out.println("Movie has been added!");
					moviecreated += 1;
					currentMovie += 1;
				}

				//Check and Create People
				String stagenameTD = rsmps.getString("stage_name");
				//stagenameTD = stagenameTD.replace("\"","");
				PreparedStatement people_ps = allConn.prepareStatement("SELECT * FROM people WHERE stage_name = ? ",ResultSet.TYPE_SCROLL_INSENSITIVE, 
                            ResultSet.CONCUR_UPDATABLE);
				people_ps.setString(1, stagenameTD);

				ResultSet srsmp = people_ps.executeQuery();
				
				//srsmp.beforeFirst();
				if (srsmp.next() == true)
				{
					System.out.println("People already exists.");
				}
				else
				{
					PreparedStatement peoplePS = allConn.prepareStatement("INSERT INTO people (stage_name) VALUES (?)");
					peoplePS.setString(1,stagenameTD);
					//peoplePS.setString(2, "Null");

					int row = peoplePS.executeUpdate();
					System.out.println("People has been added!");
					peoplecreated += 1;
				}
				
				//check and create MP
				PreparedStatement psmovie = allConn.prepareStatement("SELECT * from movies where native_name = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, 
                            ResultSet.CONCUR_UPDATABLE);
				psmovie.setString(1, nativeNameTD);
				ResultSet movieRSmps = psmovie.executeQuery();

				PreparedStatement psPeople = allConn.prepareStatement("SELECT * from people where stage_name = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, 
                            ResultSet.CONCUR_UPDATABLE);
				psPeople.setString(1, stagenameTD);
				ResultSet peopleRS = psPeople.executeQuery();
				
				movieRSmps.next();
				peopleRS.next();
				int movieIDmps = movieRSmps.getInt("movie_id");
				int peopleID = peopleRS.getInt("people_id");
                                
                                

				PreparedStatement checkMSP = allConn.prepareStatement("SELECT movies.native_name, people.stage_name FROM movies, people, mpr_test_data WHERE movies.native_name = mpr_test_data.native_name and people.stage_name = mpr_test_data.stage_name",ResultSet.TYPE_SCROLL_INSENSITIVE, 
                            ResultSet.CONCUR_UPDATABLE);
				ResultSet mpExistMps = checkMSP.executeQuery();

				PreparedStatement checkMSTable = allConn.prepareStatement("SELECT * FROM movie_people where movie_id = ? AND people_id = ?",ResultSet.TYPE_SCROLL_INSENSITIVE, 
                            ResultSet.CONCUR_UPDATABLE);
				checkMSTable.setInt(1, movieIDmps);
				checkMSTable.setInt(2, peopleID);
                                
                                
				ResultSet mpExistTableMPS = checkMSTable.executeQuery();
				
				//msExistTable.beforeFirst();

				if (mpExistTableMPS.next() == true)
				{
					System.out.println("Movie and People already exist in the movie_people table.");
				}
				
				else
				{
					String insertmoviepeople = "INSERT INTO movie_people(movie_id, people_id) VALUES (?, ?)";
					PreparedStatement moviePeopleRS = allConn.prepareStatement(insertmoviepeople);
					moviePeopleRS.setInt(1, movieIDmps);
					moviePeopleRS.setInt(2, peopleID);
                                        
                                       

					int row = moviePeopleRS.executeUpdate();
					System.out.println("MP created!");
					mpcreated += 1;
					
				}
				
				System.out.println(moviecreated);
				System.out.println(peoplecreated);
				System.out.println(mpcreated);

				if(moviecreated > 0 && peoplecreated > 0 && mpcreated > 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Created P Created MP Created' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				} 
				else {
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Ignored P Ignored MP Ignored' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}

				if (moviecreated > 0 && peoplecreated == 0 && mpcreated == 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Created P Ignored MP Ignored' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}
				
				else if(moviecreated == 0 && peoplecreated > 0 && mpcreated == 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Ignored P Created MP Ignored' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}

				else if(moviecreated == 0 && peoplecreated == 0 && mpcreated > 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Ignored P Ignored MP Created' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}

				else if(moviecreated > 0 && peoplecreated > 0 && mpcreated == 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Created P Created MP Ignored' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}

				else if(moviecreated > 0 && peoplecreated == 0 && mpcreated > 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Created P Ignored MP Created' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}

				else if(moviecreated == 0 && peoplecreated > 0 && mpcreated > 0){
					PreparedStatement movieStatusUpdateM = allConn.prepareStatement("UPDATE mpr_test_data SET execution_status = 'M Ignored P Created MP Created' WHERE stage_name = ?");
					movieStatusUpdateM.setString(1,stagenameTD);
					movieStatusUpdateM.execute();
				}

				System.out.println("\n");
			}

		allConn.close();
		}

		catch(SQLException ex){
			ex.printStackTrace();
		}
		return true;
	}
	public static void main(String[] args) {
		processMoviePeopleSong();
	}
}
	

