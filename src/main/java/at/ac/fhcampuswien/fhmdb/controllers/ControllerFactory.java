package at.ac.fhcampuswien.fhmdb.controllers;

import at.ac.fhcampuswien.fhmdb.controllers.MainController;
import at.ac.fhcampuswien.fhmdb.controllers.MovieListController;
import at.ac.fhcampuswien.fhmdb.controllers.WatchlistController;

import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object> {
    // Singleton instances of controllers
    private static MainController mainControllerInstance;
    private static MovieListController movieListControllerInstance;
    private static WatchlistController watchlistControllerInstance;


    @Override
    public Object call(Class<?> controllerClass) {
        if (controllerClass == MainController.class) {
            if (mainControllerInstance == null) {
                mainControllerInstance = new MainController();
            }
            return mainControllerInstance;
        } else if (controllerClass == MovieListController.class) {
            if (movieListControllerInstance == null) {
                movieListControllerInstance = new MovieListController();
            }
            return movieListControllerInstance;
        }else if(controllerClass == WatchlistController.class) {
            if (watchlistControllerInstance == null) {
                watchlistControllerInstance = new WatchlistController();
            }
            return watchlistControllerInstance;
        }
        try {
            // Default case: return a new instance
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
