package at.ac.fhcampuswien.fhmdb.database;

import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.observ.Observable;
import at.ac.fhcampuswien.fhmdb.observ.Observer;
import com.j256.ormlite.dao.Dao;

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
        try {
            long count = watchlistDao.queryBuilder().where().eq("apiId", movieEntity.getApiId()).countOf();
            if (count == 0) {
                int result = watchlistDao.create(movieEntity);
                MovieEntity fullMovieEntity = movieDao.queryBuilder().where().eq("apiId", movieEntity.getApiId()).queryForFirst();
                if (fullMovieEntity != null) {
                    notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), true);
                }
                return result;
            } else {
                MovieEntity fullMovieEntity = movieDao.queryBuilder().where().eq("apiId", movieEntity.getApiId()).queryForFirst();
                if (fullMovieEntity != null) {
                    notifyObservers(MovieEntity.toMovies(List.of(fullMovieEntity)).get(0), false);
                }
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseException("Error while adding to watchlist");
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
