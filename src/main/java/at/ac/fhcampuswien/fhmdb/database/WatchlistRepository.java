package at.ac.fhcampuswien.fhmdb.database;

import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.observ.Observable;
import at.ac.fhcampuswien.fhmdb.observ.Observer;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchlistRepository implements Observable {

    Dao<WatchlistMovieEntity, Long> watchlistDao;
    Dao<MovieEntity, Long> movieDao;
    private static WatchlistRepository instance;

    private final List<Observer> observers = new ArrayList<>();

    private WatchlistRepository() throws DataBaseException { // Konstruktor jetzt privat
        try {
            this.watchlistDao = DatabaseManager.getInstance().getWatchlistDao();
            this.movieDao = DatabaseManager.getInstance().getMovieDao(); // ermöglicht MovieEntity-Datenbankzugriffe
        } catch (Exception e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    public static synchronized WatchlistRepository getInstance() throws DataBaseException {
        if (instance == null) {
            instance = new WatchlistRepository();
        }
        return instance;
    }

    public List<WatchlistMovieEntity> getWatchlist() throws DataBaseException {
        try {
            return watchlistDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseException("Error while reading watchlist");
        }
    }

    public int addToWatchlist(WatchlistMovieEntity movie) throws DataBaseException {
        try {
            // Überprüfen, ob der Film bereits in der Watchlist vorhanden ist
            List<WatchlistMovieEntity> watchlist = getWatchlist();
            boolean movieExists = watchlist.stream().anyMatch(w -> w.getApiId().equals(movie.getApiId()));

            if (!movieExists) {
                // Film nur hinzufügen, wenn er noch nicht vorhanden ist
                return watchlistDao.create(movie);
            } else {
                return 0; // Film bereits vorhanden
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseException("Error while adding to watchlist");
        }
    }

    public boolean isMovieInWatchlist(String apiId) throws DataBaseException {
        try {
            long count = watchlistDao.queryBuilder().where().eq("apiId", apiId).countOf();
            return count > 0;
        } catch (SQLException e) {
            throw new DataBaseException("Error while checking if movie is in watchlist");
        }
    }

    public synchronized int removeFromWatchlist(String apiId) throws DataBaseException {
        try {
            WatchlistMovieEntity movieEntity = watchlistDao.queryBuilder().where().eq("apiId", apiId).queryForFirst();
            MovieEntity fullMovieEntity = movieDao.queryBuilder().where().eq("apiId", apiId).queryForFirst();
            int result = watchlistDao.delete(watchlistDao.queryBuilder().where().eq("apiId", apiId).query());
            if (result > 0 && movieEntity != null && fullMovieEntity != null) {
                notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), false);
            }
            return result;
        } catch (Exception e) {
            throw new DataBaseException("Error while removing from watchlist");
        }
    }

    //--------------------------------------

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Movie movie, boolean added) {
        System.out.println("Notifying observers about movie: " + movie.getTitle() + " added: " + added);
        for (Observer observer : observers) {
            observer.update(movie, added);
        }
    }
    //--------------------------------------
}
