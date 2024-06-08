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
    private static boolean isRegistered = false;

        public WatchlistController() {
            if (!isRegistered) {
                // register as observer
                // your registration code here
                isRegistered = true;
            }
        }

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
                UserDialog dialog = new UserDialog("Database Error", "Could not remove movie from watchlist");
                dialog.show();
                e.printStackTrace();
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            movieRepository = MovieRepository.getInstance();
            watchlistRepository = WatchlistRepository.getInstance();

            // Debug log to track observer initialization
            System.out.println("Initializing WatchlistController");

            if (!watchlistRepository.getObservers().contains(this)) {
                watchlistRepository.addObserver(this);
                System.out.println("WatchlistController registered as observer");
            } else {
                System.out.println("WatchlistController already registered as observer");
            }

            List<WatchlistMovieEntity> watchlist = watchlistRepository.getWatchlist();
            List<MovieEntity> movies = new ArrayList<>();

            for (WatchlistMovieEntity movie : watchlist) {
                movies.add(movieRepository.getMovie(movie.getApiId()));
            }
            observableWatchlist.clear();
            observableWatchlist.addAll(movies);
            watchlistView.setItems(observableWatchlist);
            watchlistView.setCellFactory(movieListView -> new WatchlistCell(onRemoveFromWatchlistClicked));

        } catch (DataBaseException e) {
            UserDialog dialog = new UserDialog("Database Error", "Could not read movies from DB");
            dialog.show();
            e.printStackTrace();
        }

        if (observableWatchlist.isEmpty()) {
            watchlistView.setPlaceholder(new Label("Watchlist is empty"));
        }

        System.out.println("WatchlistController initialized");
    }


    @Override
    public void update(Movie movie, boolean added) {
        try {
            MovieEntity movieEntity = movieRepository.getMovie(movie.getId());

            if (added) {
                if (!observableWatchlist.contains(movieEntity)) {
                    observableWatchlist.add(movieEntity);
                    UserDialog dialog = new UserDialog("Info", "Movie " + movie.getTitle() + " was added to the watchlist");
                    dialog.show();
                }
            } else {
                if (observableWatchlist.removeIf(m -> m.getApiId().equals(movie.getId()))) {
                    UserDialog dialog = new UserDialog("Info", "Movie " + movie.getTitle() + " was removed from the watchlist");
                    dialog.show();
                }
            }
        } catch (DataBaseException e) {
            UserDialog dialog = new UserDialog("Database Error", "Could not update watchlist");
            dialog.show();
            e.printStackTrace();
        }
    }


    // Method to unregister observer when the controller is destroyed
    public void destroy() {
        watchlistRepository.removeObserver(this);
        System.out.println("WatchlistController unregistered as observer");
    }

}
