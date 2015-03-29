package dataMining.AssignmentTwo;

import java.util.Arrays;
import java.util.List;

public class Entity {

	int UserID,Age,Occupation,movieId;
	char sex;
	double rating;
	
	List<String> genres;

	public Entity(int userID, int age, int occupation, double rating,
			String genres,int movieID) {
		
		UserID = userID;
		Age = age;
		Occupation = occupation;
		this.rating = rating;
		this.genres = Arrays.asList(genres.split("\\|"));
		this.movieId = movieID;
	}
	
	

	public Entity(double rating, List<String> genres) {
		super();
		this.rating = rating;
		this.genres = genres;
	}



	public Entity(int age, int occupation, double rating, String genres,char sex) {
		super();
		Age = age;
		Occupation = occupation;
		this.rating = rating;
		this.genres = Arrays.asList(genres.split("\\|"));
		this.sex = sex;
	}

	

	public Entity(int age, int occupation) {
		Age = age;
		Occupation = occupation;
	}

	public int getUserID() {
		return UserID;
	}

	public int getAge() {
		return Age;
	}

	public int getOccupation() {
		return Occupation;
	}

	public double getRating() {
		return rating;
	}

	public List<String> getGenres() {
		return genres;
	}

	public int getMovieId() {
		return movieId;
	}
}
     