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

    private WatchlistRepository() throws DataBaseException { // Constructor is now private
        try {
            this.watchlistDao = DatabaseManager.getInstance().getWatchlistDao();
            this.movieDao = DatabaseManager.getInstance().getMovieDao(); // Enables MovieEntity database access
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
            boolean isInWatchlist = isMovieInWatchlist(movieEntity.getApiId());
            if (!isInWatchlist) {
                int result = watchlistDao.create(movieEntity);
                if (result == 1) {
                    MovieEntity fullMovieEntity = movieDao.queryBuilder().where().eq("apiId", movieEntity.getApiId()).queryForFirst();
                    notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), true, false);
                }
                return result;
            } else {
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

    @Override
    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer added: " + observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
        System.out.println("Observer removed: " + observer);
    }

    @Override
    public void notifyObservers(Movie movie, boolean added, boolean alreadyExist) {
        System.out.println("Notifying observers...");
        for (Observer observer : observers) {
            System.out.println("Notifying observer: " + observer);
            observer.update(movie, added, alreadyExist);
        }
    }

    public List<Observer> getObservers() {
        return observers;
    }
}
