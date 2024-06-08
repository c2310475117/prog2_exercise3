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

    public synchronized int addToWatchlist(WatchlistMovieEntity movieEntity) throws DataBaseException {
        System.out.println("addToWatchlist called with movie: " + movieEntity.getApiId());
        try {
            // Überprüfen, ob der Film bereits in der Watchlist ist
            boolean isInWatchlist = isMovieInWatchlist(movieEntity.getApiId());
            if (!isInWatchlist) {
                // Film zur Watchlist hinzufügen
                int result = watchlistDao.create(movieEntity);
                if (result == 1) {
                    // Vollständigen Filmeintrag aus der Datenbank abrufen
                    MovieEntity fullMovieEntity = movieDao.queryBuilder().where().eq("apiId", movieEntity.getApiId()).queryForFirst();
                    // Observer benachrichtigen, dass ein neuer Film hinzugefügt wurde
                    notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), true, false);
                }
                return result;
            } else {
                // Der Film ist bereits in der Watchlist, keine Aktion erforderlich
                MovieEntity fullMovieEntity = movieDao.queryBuilder().where().eq("apiId", movieEntity.getApiId()).queryForFirst();
                notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), false, true);
                return 0;
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
                notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), false, false);
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

    /*
    @Override
    public void notifyObserversMovieAdded(Movie movie) {

    }

    @Override
    public void notifyObserversMovieRemoved(Movie movie) {

    }

    @Override
    public void notifyObserversMovieAlreadyExists(Movie movie) {

    }
    */

    @Override
    public void notifyObservers(Movie movie, boolean added, boolean alreadyExist) {
        for (Observer observer : observers) {
            observer.update(movie, added, alreadyExist);
        }
    }
    //--------------------------------------
}
