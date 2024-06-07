package at.ac.fhcampuswien.fhmdb.state;


import javafx.collections.ObservableList;
import at.ac.fhcampuswien.fhmdb.models.Movie;

public class UnsortedState implements SortState {
    @Override
    public void sort(ObservableList<Movie> movies) {
        // Keine Sortierung durchführen
    }
}
