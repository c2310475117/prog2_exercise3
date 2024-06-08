package at.ac.fhcampuswien.fhmdb.controllers;

import at.ac.fhcampuswien.fhmdb.ClickEventHandler;
import at.ac.fhcampuswien.fhmdb.database.*;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.observ.Observer;
import at.ac.fhcampuswien.fhmdb.ui.UserDialog;
import at.ac.fhcampuswien.fhmdb.ui.WatchlistCell;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WatchlistController implements Initializable, Observer {

    private static WatchlistController instance;
    private MovieRepository movieRepository;
    private WatchlistRepository watchlistRepository;

    public static synchronized WatchlistController getInstance() {
        if (instance == null) {
            instance = new WatchlistController();
        }
        return instance;
    }
    @FXML
    private JFXListView<MovieEntity> watchlistView;

    protected ObservableList<MovieEntity> observableWatchlist = FXCollections.observableArrayList();



    private final ClickEventHandler onRemoveFromWatchlistClicked = (o) -> {
        if (o instanceof MovieEntity) {
            MovieEntity movieEntity = (MovieEntity) o;

            try {
                watchlistRepository.removeFromWatchlist(movieEntity.getApiId());
                observableWatchlist.remove(movieEntity);

                // Convert MovieEntity to Movie before notifying observers
                // Movie movie = MovieEntity.toMovies(List.of(movieEntity)).get(0);
                // watchlistRepository.notifyObservers(movie, false, true);
            } catch (DataBaseException e) {
                // UserDialog dialog = new UserDialog("Database Error", "Could not remove movie from watchlist");
                //dialog.show();
                e.printStackTrace();
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            movieRepository = MovieRepository.getInstance();
            watchlistRepository = WatchlistRepository.getInstance();
            // watchlistRepository.addObserver(this);

            List<WatchlistMovieEntity> watchlist = watchlistRepository.getWatchlist();
            List<MovieEntity> movies = new ArrayList<>();

            for (WatchlistMovieEntity watchlistMovie : watchlist) {
                MovieEntity movieEntity = movieRepository.getMovie(watchlistMovie.getApiId());
                if (movieEntity != null) {
                    movies.add(movieEntity);
                }
            }
            observableWatchlist.clear();
            observableWatchlist.addAll(movies);
            watchlistView.setItems(observableWatchlist);
            watchlistView.setCellFactory(movieListView -> new WatchlistCell(onRemoveFromWatchlistClicked));

        } catch (DataBaseException e) {
            // UserDialog dialog = new UserDialog("Database Error", "Could not read movies from DB");
            // dialog.show();
            e.printStackTrace();
        }

        if (observableWatchlist.isEmpty()) {
            watchlistView.setPlaceholder(new Label("Watchlist is empty"));
        }

        System.out.println("WatchlistController initialized");
    }

/*
    @Override
    public void update(Movie movie, boolean added) {
        try {
            MovieEntity movieEntity = movieRepository.getMovie(movie.getId());

            if (added) {
                observableWatchlist.add(movieEntity);
            } else {
                observableWatchlist.removeIf(m -> m.getApiId().equals(movie.getId()));
            }
        } catch (DataBaseException e) {
            UserDialog dialog = new UserDialog("Database Error", "Could not update watchlist");
            dialog.show();
            e.printStackTrace();
        }
    }
*/
    @Override
    public void update (Movie movie, boolean added, boolean alreadyExist) {
        String message;
    if (alreadyExist) {
        message = "Movie " + movie.getTitle() + " already exists in the watchlist";
    } else {
        message = "Movie " + movie.getTitle() + (added ? " was added to" : " was removed from") + " the watchlist";
    }
    UserDialog dialog = new UserDialog("Info", message);
    dialog.show();
    }
}
