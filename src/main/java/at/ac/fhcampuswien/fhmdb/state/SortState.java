package at.ac.fhcampuswien.fhmdb.state;

import at.ac.fhcampuswien.fhmdb.models.Movie;
import javafx.collections.ObservableList;


public interface SortState {
    void sort(ObservableList<Movie> movies);
}
