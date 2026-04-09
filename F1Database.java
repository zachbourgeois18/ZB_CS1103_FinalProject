/***************************************************************
 * CS 1103 Final Project
 * Database representive Formula 1 point standings
 * Utilization of JDBC + SQLite
 * 
 * The program is designed from the perspective of a Formula 1
 * race steward or official responsible for managing race results
 * and maintaining championship standings throughout the course of a
 * season. The steward can input race results, and
 * applies changes such as disqualifications when necessary.
 * 
 * This program also has extra features such as being able to view
 * championship standings and the races in a year to make it easier
 * for the user/stewart using the program.
 * 
 *
 * @author Zachary Bourgeois
 * @version April 10th, 2026
 **************************************************************/
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class F1Database
{
    public static void main(String[] args)
    {
        Connection conn = null;
        try
        {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to database file
            conn = DriverManager.getConnection("jdbc:sqlite:f1db.db");

            // Create statement object
            Statement stmt = conn.createStatement();
            
            //Lets you rerun the program without errors in its initial state
            stmt.executeUpdate("DROP TABLE IF EXISTS drivers");
            stmt.executeUpdate("DROP TABLE IF EXISTS teams");
            stmt.executeUpdate("DROP TABLE IF EXISTS races");
            stmt.executeUpdate("DROP TABLE IF EXISTS raceResults");

            // CREATES TABLES
            createTeams(stmt);//teams
            createDrivers(stmt);//drivers
            createRaces(stmt);//races
            createRaceResults(stmt);//raceResults

            // INSERT values into the TABLES
            insertValuesTeams(stmt);//teams
            insertValuesDrivers(stmt);//drivers
            insertRace(stmt);
            //insertRace3Points(stmt);//drivers and teams, inserts points as of Race 3, method does not use RaceResults table
            
            
            //Method that runs the program
            System.out.println("Choose the following option you would like by entering in the corresponding number (1-6)");
            startProgram(stmt);
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("JDBC driver not found.");
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            System.out.println("SQL error.");
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static void startProgram(Statement stmt) throws SQLException
    {
        boolean stop = false;
        
        Scanner input = new Scanner(System.in);

        while(!stop)
        {
            System.out.println("\n1. View championship standings");
            System.out.println("2. View race calendar");
            System.out.println("3. View race result");
            System.out.println("4. Input race results");
            System.out.println("5. Disqualify a driver");
            System.out.println("6. Exit");
        
            int choice = input.nextInt();
        
            if (choice == 1)
            {
                printChampionshipStandings(stmt);
            }
        
            else if (choice == 2)
            {
                printRaces(stmt);
            }
            
            else if (choice == 3)
            {
                printRaceResult(stmt);
            }
            
            else if (choice == 4)
            {
                insertDriverResults(stmt);
            }
            
            else if (choice == 5)
            {
                disqualifyDriver(stmt);
            }
        
            else if (choice == 6)
            {
                stop = true;
            }
        
            else
            {
                System.out.println("Invalid choice, please try again");
            }
        }
        
        input.close();
    }
     
    /*******************************TABLE CREATION METHODS************************************/
    //Method to create teams table
    public static void createTeams(Statement stmt) throws SQLException
    {
        stmt.executeUpdate(
                "CREATE TABLE teams (" +
                "team_id INTEGER PRIMARY KEY, " +
                "name TEXT, " +
                "country TEXT, " +
                "points INTEGER)"
            );
    }

    //Method to create drivers table
    public static void createDrivers(Statement stmt) throws SQLException
    {
        stmt.executeUpdate(
            "CREATE TABLE drivers (" +
            "driver_id INTEGER PRIMARY KEY, " +
            "name TEXT NOT NULL, " +
            "nationality TEXT, " +
            "number INTEGER UNIQUE, " +
            "points INTEGER, " +
            "team_id INTEGER, " +
            "FOREIGN KEY (team_id) REFERENCES teams(team_id))"
        );
    }
    
    //Method to create races table
    public static void createRaces(Statement stmt) throws SQLException
    {
        stmt.executeUpdate(
            "CREATE TABLE races (" +
            "race_id INTEGER PRIMARY KEY, " +
            "race_name TEXT NOT NULL, " +
            "track_name TEXT NOT NULL, " +
            "location TEXT NOT NULL)"
        );
    }
    
    //Method to create race results table
    public static void createRaceResults(Statement stmt) throws SQLException
    {
        stmt.executeUpdate(
            "CREATE TABLE raceResults (" +
            "result_id INTEGER PRIMARY KEY, " +
            "race_id INTEGER NOT NULL, " +
            "driver_id INTEGER NOT NULL, " +
            "position INTEGER, " +
            "points_earned INTEGER, " +
            "FOREIGN KEY (race_id) REFERENCES races(race_id), " +
            "FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)," + 
            "UNIQUE (race_id, driver_id)," + 
            "UNIQUE (race_id, position))"
        );
    }
    
    /*****************************INSERT VALUE INTO TABLES METHODS*****************************/
    //Method to insert values into teams table
    public static void insertValuesTeams(Statement stmt) throws SQLException
    {
        stmt.executeUpdate("INSERT INTO teams VALUES (1, 'Ferrari', 'Italy', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (2, 'McLaren', 'UK', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (3, 'Williams', 'UK', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (4, 'Mercedes', 'Germany', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (5, 'Red Bull Racing', 'Austria', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (6, 'Aston Martin', 'UK', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (7, 'Alpine', 'France', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (8, 'Haas', 'USA', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (9, 'Racing Bulls', 'Italy', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (10, 'Audi', 'Germany', 0)");
        stmt.executeUpdate("INSERT INTO teams VALUES (11, 'Cadillac', 'USA', 0)");
    }
    
    //Updates the team points based on the drivers championship
    public static void updateTeamPoints(Statement stmt) throws SQLException
    {
        stmt.executeUpdate(
            "UPDATE teams SET points = (" +
            "SELECT COALESCE(SUM(points), 0) FROM drivers " +
            "WHERE drivers.team_id = teams.team_id)"
        );
    }
    
    //Updates the driver points based on past RaceResults up to date
    public static void updateDriverPoints(Statement stmt) throws SQLException
    {
        stmt.executeUpdate(
        "UPDATE drivers SET points = (" +
        "SELECT COALESCE(SUM(points_earned), 0) " +
        "FROM raceResults " +
        "WHERE raceResults.driver_id = drivers.driver_id" +
        ")"
        );
    }

    //Method to insert values into drivers table
    public static void insertValuesDrivers(Statement stmt) throws SQLException
    {
        stmt.executeUpdate("INSERT INTO drivers VALUES (1, 'Lewis Hamilton', 'UK', 44, 0, 1)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (2, 'Charles Leclerc', 'Monaco', 16, 0, 1)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (3, 'Lando Norris', 'UK', 1, 0, 2)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (4, 'Oscar Piastri', 'Australia', 81, 0, 2)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (5, 'Carlos Sainz', 'Spain', 55, 0, 3)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (6, 'Alexander Albon', 'Thailand', 23, 0, 3)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (7, 'George Russell', 'UK', 63, 0, 4)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (8, 'Kimi Antonelli', 'Italy', 12, 0, 4)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (9, 'Max Verstappen', 'Netherlands', 3, 0, 5)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (10, 'Isack Hadjar', 'France', 6, 0, 5)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (11, 'Fernando Alonso', 'Spain', 14, 0, 6)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (12, 'Lance Stroll', 'Canada', 18, 0, 6)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (13, 'Pierre Gasly', 'France', 10, 0, 7)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (14, 'Franco Colapinto', 'Argentina', 43, 0, 7)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (15, 'Esteban Ocon', 'France', 31, 0, 8)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (16, 'Oliver Bearman', 'UK', 87, 0, 8)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (17, 'Liam Lawson', 'New Zealand', 30, 0, 9)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (18, 'Arvid Lindblad', 'UK', 41, 0, 9)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (19, 'Nico Hulkenberg', 'Germany', 27, 0, 10)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (20, 'Gabriel Bortoleto', 'Brazil', 5, 0, 10)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (21, 'Sergio Perez', 'Mexico', 11, 0, 11)");
        stmt.executeUpdate("INSERT INTO drivers VALUES (22, 'Valtteri Bottas', 'Finland', 77, 0, 11)");
    }
    
    //Updates the championship based on Race 3 of the 2026 f1 season
    public static void insertRace3Points(Statement stmt) throws SQLException
    {
        stmt.executeUpdate("UPDATE drivers SET points = 72 WHERE name = 'Kimi Antonelli'");
        stmt.executeUpdate("UPDATE drivers SET points = 63 WHERE name = 'George Russell'");
        stmt.executeUpdate("UPDATE drivers SET points = 49 WHERE name = 'Charles Leclerc'");
        stmt.executeUpdate("UPDATE drivers SET points = 41 WHERE name = 'Lewis Hamilton'");
        stmt.executeUpdate("UPDATE drivers SET points = 25 WHERE name = 'Lando Norris'");
        stmt.executeUpdate("UPDATE drivers SET points = 21 WHERE name = 'Oscar Piastri'");
        stmt.executeUpdate("UPDATE drivers SET points = 17 WHERE name = 'Oliver Bearman'");
        stmt.executeUpdate("UPDATE drivers SET points = 15 WHERE name = 'Pierre Gasly'");
        stmt.executeUpdate("UPDATE drivers SET points = 12 WHERE name = 'Max Verstappen'");
        stmt.executeUpdate("UPDATE drivers SET points = 10 WHERE name = 'Liam Lawson'");
        stmt.executeUpdate("UPDATE drivers SET points = 4 WHERE name = 'Arvid Lindblad'");
        stmt.executeUpdate("UPDATE drivers SET points = 4 WHERE name = 'Isack Hadjar'");
        stmt.executeUpdate("UPDATE drivers SET points = 2 WHERE name = 'Gabriel Bortoleto'");
        stmt.executeUpdate("UPDATE drivers SET points = 2 WHERE name = 'Carlos Sainz'");
        stmt.executeUpdate("UPDATE drivers SET points = 1 WHERE name = 'Esteban Ocon'");
        stmt.executeUpdate("UPDATE drivers SET points = 1 WHERE name = 'Franco Colapinto'");
            
        updateTeamPoints(stmt);//Updates the team points
    }
    
    //Method to insert values into races table
    public static void insertRace(Statement stmt) throws SQLException
    {
        stmt.executeUpdate("INSERT INTO races VALUES (1, 'Australian Grand Prix', 'Albert Park Circuit', 'Melbourne, Australia')");
        stmt.executeUpdate("INSERT INTO races VALUES (2, 'Chinese Grand Prix', 'Shanghai International Circuit', 'Shanghai, China')");
        stmt.executeUpdate("INSERT INTO races VALUES (3, 'Japanese Grand Prix', 'Suzuka Circuit', 'Suzuka, Japan')");
        stmt.executeUpdate("INSERT INTO races VALUES (4, 'Bahrain Grand Prix', 'Bahrain International Circuit', 'Sakhir, Bahrain')");
        stmt.executeUpdate("INSERT INTO races VALUES (5, 'Saudi Arabian Grand Prix', 'Jeddah Corniche Circuit', 'Jeddah, Saudi Arabia')");
        stmt.executeUpdate("INSERT INTO races VALUES (6, 'Miami Grand Prix', 'Miami International Autodrome', 'Miami, USA')");
        stmt.executeUpdate("INSERT INTO races VALUES (7, 'Emilia Romagna Grand Prix', 'Imola Circuit', 'Imola, Italy')");
        stmt.executeUpdate("INSERT INTO races VALUES (8, 'Monaco Grand Prix', 'Circuit de Monaco', 'Monaco')");
        stmt.executeUpdate("INSERT INTO races VALUES (9, 'Canadian Grand Prix', 'Circuit Gilles Villeneuve', 'Montreal, Canada')");
        stmt.executeUpdate("INSERT INTO races VALUES (10, 'Spanish Grand Prix', 'Circuit de Barcelona-Catalunya', 'Barcelona, Spain')");
        stmt.executeUpdate("INSERT INTO races VALUES (11, 'Austrian Grand Prix', 'Red Bull Ring', 'Spielberg, Austria')");
        stmt.executeUpdate("INSERT INTO races VALUES (12, 'British Grand Prix', 'Silverstone Circuit', 'Silverstone, UK')");
        stmt.executeUpdate("INSERT INTO races VALUES (13, 'Hungarian Grand Prix', 'Hungaroring', 'Budapest, Hungary')");
        stmt.executeUpdate("INSERT INTO races VALUES (14, 'Belgian Grand Prix', 'Circuit de Spa-Francorchamps', 'Spa, Belgium')");
        stmt.executeUpdate("INSERT INTO races VALUES (15, 'Dutch Grand Prix', 'Circuit Zandvoort', 'Zandvoort, Netherlands')");
        stmt.executeUpdate("INSERT INTO races VALUES (16, 'Italian Grand Prix', 'Monza Circuit', 'Monza, Italy')");
        stmt.executeUpdate("INSERT INTO races VALUES (17, 'Azerbaijan Grand Prix', 'Baku City Circuit', 'Baku, Azerbaijan')");
        stmt.executeUpdate("INSERT INTO races VALUES (18, 'Singapore Grand Prix', 'Marina Bay Street Circuit', 'Singapore')");
        stmt.executeUpdate("INSERT INTO races VALUES (19, 'United States Grand Prix', 'Circuit of the Americas', 'Austin, USA')");
        stmt.executeUpdate("INSERT INTO races VALUES (20, 'Mexico City Grand Prix', 'Autodromo Hermanos Rodriguez', 'Mexico City, Mexico')");
        stmt.executeUpdate("INSERT INTO races VALUES (21, 'Brazilian Grand Prix', 'Interlagos Circuit', 'Sao Paulo, Brazil')");
        stmt.executeUpdate("INSERT INTO races VALUES (22, 'Las Vegas Grand Prix', 'Las Vegas Strip Circuit', 'Las Vegas, USA')");
        stmt.executeUpdate("INSERT INTO races VALUES (23, 'Qatar Grand Prix', 'Lusail International Circuit', 'Lusail, Qatar')");
        stmt.executeUpdate("INSERT INTO races VALUES (24, 'Abu Dhabi Grand Prix', 'Yas Marina Circuit', 'Abu Dhabi, UAE')");
    }
    
    //Inserts points for drivers based on race results
    public static void insertDriverResults(Statement stmt) throws SQLException
    {
        Scanner input = new Scanner(System.in);
        
        boolean inputDriver = true;
        
        while(inputDriver)
        {
            printRaces(stmt);
            System.out.println("\nEnter the race you would like to input results for");
            int raceId = input.nextInt();
        
            int[] pointsTable = {25, 18, 15, 12, 10, 8, 6, 4, 2, 1};
        
            printDriverId(stmt);
            System.out.print("\nUsing the IDs shown above, input the corresponding driver into each points scoring position (1-10)");
        
            for(int i = 0; i < 10; i++)
            {
                System.out.print("\nInput the driver who finished in position " + (i + 1) + ": ");
                int driverId = input.nextInt();
        
                stmt.executeUpdate(
                    "INSERT INTO raceResults (race_id, driver_id, position, points_earned) VALUES (" +
                    raceId + ", " + driverId + ", " + (i + 1) + ", " + pointsTable[i] + ")"
                );// Adds points into RaceResults table
        
                System.out.println(pointsTable[i] + " points have successfully been added to driver #" + driverId);
            }
        
            System.out.println("\nPlease enter \"stop\" if you would like to return to choices");
            System.out.println("Otherwise, input anything else to continue");
        
            String answer = input.next();
        
            if(answer.equalsIgnoreCase("stop"))
            {
                updateDriverPoints(stmt);
                updateTeamPoints(stmt);
                inputDriver = false;
            }
        }        
    }
    
    //Disqualifies a driver from race
    //NOTE: Normally,  a disqualification shifts every driver upwards a position, however for the sake of complexity, we could simply reinput race results if absolutely necessary, this method is to showcase "DELETE" in SQLite
    public static void disqualifyDriver(Statement stmt) throws SQLException
    {
        Scanner input = new Scanner(System.in);
        
        printRaces(stmt);
        System.out.print("\nEnter race ID: ");
        int raceId = input.nextInt();
    
        printDriverId(stmt);
        System.out.print("Enter driver ID to disqualify: ");
        int driverId = input.nextInt();
    
        stmt.executeUpdate(
            "DELETE FROM raceResults WHERE race_id = " + raceId +
            " AND driver_id = " + driverId
        );
        
        System.out.println("Driver #" + driverId + " removed from race " + raceId);
        
        // Recalculate standings
        updateDriverPoints(stmt);
        updateTeamPoints(stmt);
    }
    
    
    /*************************PRINTING METHODS********************************/
    
    //Prints the championship
    public static void printChampionshipStandings(Statement stmt) throws SQLException
    {
        //Printing the drivers standings
        System.out.println("\n------------------------- 2026 Drivers' Standings -------------------------");

        System.out.printf("%18s %13s %8s %8s %18s\n",
                          "Name", "Nationality", "Number", "Points", "Team");
            
        ResultSet rs = stmt.executeQuery(
        "SELECT drivers.name, drivers.nationality, drivers.number, drivers.points, " +
        "teams.name AS team " +
        "FROM drivers " +
        "JOIN teams ON drivers.team_id = teams.team_id " +
        "ORDER BY drivers.points DESC");  
        
        while (rs.next())
        {
            System.out.printf("%18s %13s %8s %8s %18s\n",
                rs.getString("name"),
                rs.getString("nationality"),
                rs.getInt("number"),
                rs.getInt("points"),
                rs.getString("team")
             );
        }

        //Printing the team standings
        System.out.println();
        System.out.println("------------------------- 2026 Teams' Standings -------------------------");
        rs = stmt.executeQuery("SELECT * FROM teams ORDER BY points DESC");
        
        System.out.printf("%18s %13s %8s\n",
                         "Name", "Nationality", "Points");
        while (rs.next())
        {
            System.out.printf("%18s %13s %8d\n",
                rs.getString("name"), 
                rs.getString("country"),
                rs.getInt("points")
            );
        }
    }
    
    //Prints all 24 races of the 2026 season
    public static void printRaces(Statement stmt) throws SQLException
    {
        ResultSet rs = stmt.executeQuery("SELECT race_id, race_name, location FROM races");
    
        System.out.println("\n--- Race Calendar ---");
        while (rs.next())
        {
            System.out.println(rs.getInt("race_id") + ": " +
                               rs.getString("race_name") + " (" +
                               rs.getString("location") + ")");
        }
    }
    
    //Prints the result of a race
    public static void printRaceResult(Statement stmt) throws SQLException
    {
        Scanner input = new Scanner(System.in);
    
        printRaces(stmt);
        System.out.print("\nEnter race ID: ");
        int raceId = input.nextInt();
    
        ResultSet rs = stmt.executeQuery(
            "SELECT raceResults.position, drivers.name " +
            "FROM raceResults " +
            "JOIN drivers ON raceResults.driver_id = drivers.driver_id " +
            "WHERE raceResults.race_id = " + raceId + " " +
            "ORDER BY raceResults.position ASC"
        );
    
        boolean hasResults = false;
    
        System.out.println("\n--- Race Results ---");
    
        while (rs.next())
        {
            hasResults = true;
    
            System.out.println(
                "Position " + rs.getInt("position") + ": " +
                rs.getString("name")
            );
        }
    
        if (!hasResults)
        {
            System.out.println("This race has no results yet.");
        }
    }
    
    //Prints all of the drivers in order of their driver id
    public static void printDriverId(Statement stmt) throws SQLException
    {
        ResultSet rs = stmt.executeQuery("SELECT name, driver_id FROM drivers ORDER BY driver_id");
    
        System.out.println("\n----------Drivers----------");
        while (rs.next())
        {
            System.out.println(rs.getString("name") + " ID: " + rs.getInt("driver_id"));
        }
    }
}