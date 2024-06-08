package at.ac.fhcampuswien.fhmdb.controllers;

import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object> {
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
        } else if (controllerClass == WatchlistController.class) {
            if (watchlistControllerInstance == null) {
                watchlistControllerInstance = WatchlistController.getInstance();
            }
            return watchlistControllerInstance;
        }
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
