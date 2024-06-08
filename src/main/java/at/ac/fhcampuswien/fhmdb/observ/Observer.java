package at.ac.fhcampuswien.fhmdb.observ;

import at.ac.fhcampuswien.fhmdb.models.Movie;

public interface Observer {

    void update(Movie movie, boolean added, boolean alreadyExist);
}