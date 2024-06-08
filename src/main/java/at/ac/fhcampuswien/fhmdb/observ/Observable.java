package at.ac.fhcampuswien.fhmdb.observ;

import at.ac.fhcampuswien.fhmdb.models.Movie;

public interface Observable {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);

    /*
    void notifyObserversMovieAdded(Movie movie);
    void notifyObserversMovieRemoved(Movie movie);
    void notifyObserversMovieAlreadyExists(Movie movie);
    */

    void notifyObservers(Movie movie, boolean added, boolean alreadyexist);
}