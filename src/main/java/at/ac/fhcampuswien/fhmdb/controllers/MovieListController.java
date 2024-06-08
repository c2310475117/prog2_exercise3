package at.ac.fhcampuswien.fhmdb.controllers;

import at.ac.fhcampuswien.fhmdb.ClickEventHandler;
import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.api.MovieApiException;
import at.ac.fhcampuswien.fhmdb.database.*;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.observ.Observer;
import at.ac.fhcampuswien.fhmdb.state.AscendingState;
import at.ac.fhcampuswien.fhmdb.state.DescendingState;
import at.ac.fhcampuswien.fhmdb.state.SortState;
import at.ac.fhcampuswien.fhmdb.state.UnsortedState;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import at.ac.fhcampuswien.fhmdb.ui.UserDialog;
import com.j256.ormlite.dao.Dao;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MovieListController implements Initializable, Observer {

    @FXML
    public JFXButton searchBtn;

    @FXML
    public TextField searchField;

    @FXML
    public JFXListView movieListView;

    @FXML
    public JFXComboBox genreComboBox;

    @FXML
    public JFXComboBox releaseYearComboBox;

    @FXML
    public JFXComboBox ratingFromComboBox;

    @FXML
    public JFXButton sortBtn;

    public List<Movie> allMovies;

    protected ObservableList<Movie> observableMovies = FXCollections.observableArrayList();

    protected List<Movie> unsortedMovies;

    private SortState currentState;

    private MovieRepository movieRepository;

    private WatchlistRepository watchlistRepository;

    private boolean isAddingToWatchlist = false;

    Dao<WatchlistMovieEntity, Long> watchlistDao;


    private final ClickEventHandler onAddToWatchlistClicked = (clickedItem) -> {
        synchronized (this) {
            System.out.println("EventHandler aufgerufen. isAddingToWatchlist: " + isAddingToWatchlist); // Logging hinzufügen
            if (!isAddingToWatchlist && clickedItem instanceof Movie movie) {
                isAddingToWatchlist = true;
                System.out.println("Beginne mit dem Hinzufügen zum Watchlist."); // Logging hinzufügen

                try {
                    String apiId = movie.getId();
                    WatchlistMovieEntity watchlistMovieEntity = new WatchlistMovieEntity(apiId);
                    watchlistRepository.addToWatchlist(watchlistMovieEntity);
                    // Keine Benachrichtigungen werden gesendet, unabhängig vom Ergebnis
                } catch (DataBaseException e) {
                    e.printStackTrace();
                } finally {
                    isAddingToWatchlist = false;
                    System.out.println("Hinzufügen zum Watchlist abgeschlossen. isAddingToWatchlist zurückgesetzt."); // Logging hinzufügen
                }
            }
        }
    };



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FXMLLoader loader = new FXMLLoader(MovieListController.class.getResource("movie-list.fxml"));
        loader.setControllerFactory(new ControllerFactory());

        try {
            movieRepository = MovieRepository.getInstance();
            watchlistRepository = WatchlistRepository.getInstance();
            watchlistDao = DatabaseManager.getInstance().getWatchlistDao();

            watchlistRepository.addObserver(this);

        } catch (DataBaseException e) {
            throw new RuntimeException(e);
        }

        initializeState();
        initializeLayout();

        currentState = new UnsortedState();

    }

    public void initializeState() {
        List<Movie> result;
        try {
            result = MovieAPI.getAllMovies();
            writeCache(result);
        } catch (MovieApiException e) {
            UserDialog dialog = new UserDialog("MovieAPI Error", "Could not load movies from API. Get movies from DB cache instead.");
            dialog.show();
            result = readCache();
        }

        setMovies(result);
        setMovieList(result);
    }

    private List<Movie> readCache() {
        try {
            return MovieEntity.toMovies(movieRepository.getAllMovies());
        } catch (DataBaseException e) {
            UserDialog dialog = new UserDialog("DB Error", "Could not load movies from DB.");
            dialog.show();
            return new ArrayList<>();
        }
    }

    private void writeCache(List<Movie> movies) {
        try {
            movieRepository.removeAll();
            movieRepository.addAllMovies(movies);
        } catch (DataBaseException e) {
            UserDialog dialog = new UserDialog("DB Error", "Could not write movies to DB.");
            dialog.show();
        }
    }

    public void initializeLayout() {
        movieListView.setItems(observableMovies);
        movieListView.setCellFactory(movieListView -> new MovieCell(onAddToWatchlistClicked));

        genreComboBox.getItems().add("No filter");
        genreComboBox.getItems().addAll(Genre.values());
        genreComboBox.setPromptText("Filter by Genre");

        releaseYearComboBox.getItems().add("No filter");
        for (int i = 1900; i <= 2023; i++) {
            releaseYearComboBox.getItems().add(i);
        }
        releaseYearComboBox.setPromptText("Filter by Release Year");

        ratingFromComboBox.getItems().add("No filter");
        for (int i = 0; i <= 10; i++) {
            ratingFromComboBox.getItems().add(i);
        }
        ratingFromComboBox.setPromptText("Filter by Rating");
    }

    public void setMovies(List<Movie> movies) {
        allMovies = movies;
    }

    public void setMovieList(List<Movie> movies) {
        observableMovies.clear();
        observableMovies.addAll(movies);
        unsortedMovies = movies;
    }

    public void sortMovies() {
        if (currentState instanceof UnsortedState) {
            currentState = new AscendingState();
        } else if (currentState instanceof AscendingState) {
            currentState = new DescendingState();
        } else if (currentState instanceof DescendingState) {
            currentState = new UnsortedState();
            observableMovies.clear();
            observableMovies.addAll(unsortedMovies);
        }
        currentState.sort(observableMovies);
    }

    public List<Movie> filterByQuery(List<Movie> movies, String query) {
        if (query == null || query.isEmpty()) return movies;

        return movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        movie.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Movie> filterByGenre(List<Movie> movies, Genre genre) {
        if (genre == null) return movies;

        return movies.stream()
                .filter(movie -> movie.getGenres().contains(genre))
                .toList();
    }

    public void applyAllFilters(String searchQuery, Object genre) {
        List<Movie> filteredMovies = allMovies;

        if (!searchQuery.isEmpty()) {
            filteredMovies = filterByQuery(filteredMovies, searchQuery);
        }

        if (genre != null && !genre.toString().equals("No filter")) {
            filteredMovies = filterByGenre(filteredMovies, Genre.valueOf(genre.toString()));
        }

        observableMovies.clear();
        observableMovies.addAll(filteredMovies);
        unsortedMovies = filteredMovies;
    }

    public void searchBtnClicked(ActionEvent actionEvent) {
        String searchQuery = searchField.getText().trim().toLowerCase();
        String releaseYear = validateComboboxValue(releaseYearComboBox.getSelectionModel().getSelectedItem());
        String ratingFrom = validateComboboxValue(ratingFromComboBox.getSelectionModel().getSelectedItem());
        String genreValue = validateComboboxValue(genreComboBox.getSelectionModel().getSelectedItem());

        Genre genre = genreValue != null ? Genre.valueOf(genreValue) : null;

        List<Movie> movies = getMovies(searchQuery, genre, releaseYear, ratingFrom);

        setMovies(movies);
        setMovieList(movies);
        currentState.sort(observableMovies);
    }

    public String validateComboboxValue(Object value) {
        return value != null && !value.toString().equals("No filter") ? value.toString() : null;
    }

    public List<Movie> getMovies(String searchQuery, Genre genre, String releaseYear, String ratingFrom) {
        try {
            return MovieAPI.getAllMovies(searchQuery, genre, releaseYear, ratingFrom);
        } catch (MovieApiException e) {
            System.out.println(e.getMessage());
            UserDialog dialog = new UserDialog("MovieAPI Error", "Could not load movies from API.");
            dialog.show();
            return new ArrayList<>();
        }
    }

    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    @Override
    public void update(Movie movie, boolean added, boolean alreadyExist) {
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
