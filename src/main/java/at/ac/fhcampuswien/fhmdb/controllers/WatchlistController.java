package at.ac.fhcampuswien.fhmdb.controllers;

import at.ac.fhcampuswien.fhmdb.ClickEventHandler;
import at.ac.fhcampuswien.fhmdb.database.DataBaseException;
import at.ac.fhcampuswien.fhmdb.database.MovieEntity;
import at.ac.fhcampuswien.fhmdb.database.MovieRepository;
import at.ac.fhcampuswien.fhmdb.database.WatchlistMovieEntity;
import at.ac.fhcampuswien.fhmdb.database.WatchlistRepository;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.observ.Observer;
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
            } catch (DataBaseException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            movieRepository = MovieRepository.getInstance();
            watchlistRepository = WatchlistRepository.getInstance();

            // Ensure observer is only added once
            if (!watchlistRepository.getObservers().contains(this)) {
                watchlistRepository.addObserver(this);
                System.out.println("Observer added in WatchlistController");
            }

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
            e.printStackTrace();
        }

        if (observableWatchlist.isEmpty()) {
            watchlistView.setPlaceholder(new Label("Watchlist is empty"));
        }

        System.out.println("WatchlistController initialized");
    }

    @Override
    public void update(Movie movie, boolean added, boolean alreadyExist) {
        // Do not show dialog in WatchlistController
        System.out.println("WatchlistController update: Movie " + movie.getTitle() + (added ? " added to" : " removed from") + " the watchlist");
    }
}
