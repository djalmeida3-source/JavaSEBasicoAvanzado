package com.anncode.amazonviewer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.anncode.amazonviewer.db.DataBase.*;

import com.anncode.amazonviewer.db.IDBConnection;
import com.anncode.amazonviewer.model.Movie;

public interface MovieDAO extends IDBConnection {
	
	default Movie setMovieViewed(Movie movie) {
		try(Connection connection = connectToDB()){
			Statement statement = connection.createStatement();
			String query = "INSERT INTO " + TVIEWED + 
					" ("+TVIEWED_IDMATERIAL+", "+TVIEWED_IDELEMENT+", "+TVIEWED_IDUSUARIO+", "+TVIEWED_DATE+")" +
					 " VALUES("+ID_TMATERIALS [0]+", "+movie.getId()+", "+TUSER_IDUSUARIO+", "+new Date()+")";
			if (statement.executeUpdate(query) > 0) {
				System.out.println("Se marcó en Visto");
			}
			
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return movie;
	} 
	
	default ArrayList<Movie> read(){
		ArrayList<Movie> movies = new ArrayList();
		try (Connection connection = connectToDB()){
			String query = "SELECT * FROM " + TMOVIE;
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				Movie movie = new Movie(
						rs.getString(TMOVIE_TITLE), 
						rs.getString(TMOVIE_GENRE), 
						rs.getString(TMOVIE_CREATOR), 
						Integer.valueOf(rs.getString(TMOVIE_DURATION)), 
						Short.valueOf(rs.getString(TMOVIE_YEAR)));
				
				movie.setId(Integer.valueOf(rs.getString(TMOVIE_ID)));
				movie.setViewed(getMovieViewed(
						preparedStatement, 
						connection, 
						Integer.valueOf(rs.getString(TMOVIE_ID))));
				movies.add(movie);
				
			}
			
			
		}catch (SQLException e) {
			// TODO: handle exception
		}
		return movies;
	}
	
	private boolean getMovieViewed(PreparedStatement preparedStatement, Connection connection, int id_movie) {
		boolean viewed = false;
		String query = "SELECT * FROM " + TVIEWED +
				" WHERE " + TVIEWED_IDMATERIAL + "= ?"+ 
				" AND " + TVIEWED_IDELEMENT + "= ?" +
				" AND " + TVIEWED_IDUSUARIO + "= ?";
		ResultSet rs = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, ID_TMATERIALS[0]);
			preparedStatement.setInt(2, id_movie);
			preparedStatement.setInt(3, TUSER_IDUSUARIO);
			
			rs = preparedStatement.executeQuery();
			viewed = rs.next();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return viewed;
	}

	@SuppressWarnings("finally")
	default ArrayList<Movie> getMoviesViewedByDate(Date date) {
		ArrayList<Movie> movies = new ArrayList<>();
		String dateFormat = new SimpleDateFormat("yyyy-MM-dd").format(date);

		try (Connection connection = connectToDB()) {
			String query = "SELECT * FROM " + TMOVIE + " AS m INNER JOIN " + TVIEWED +
					" AS v ON m." + TMOVIE_ID + " = v." + TVIEWED_IDELEMENT +
					" WHERE v." + TVIEWED_IDMATERIAL + " = " + ID_TMATERIALS[0] +
					" AND v." + TVIEWED_IDUSUARIO + " = " + TUSER_IDUSUARIO +
					" AND DATE(v.`" + TVIEWED_DATE + "`) = '" + dateFormat + "';";

			PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				Movie movie = new Movie(
						resultSet.getString("m." + TMOVIE_TITLE),
						resultSet.getString("m." + TMOVIE_GENRE),
						resultSet.getString("m." + TMOVIE_CREATOR),
						resultSet.getInt("m." + TMOVIE_DURATION),
						resultSet.getShort("m." + TMOVIE_YEAR));

				movie.setId(resultSet.getInt("m." + TMOVIE_ID));
				movie.setViewed(true);
				movies.add(movie);
			}

			System.out.println(movies.size());

			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			return movies;
		}
	}
	
}












