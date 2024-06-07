package at.ac.fhcampuswien.fhmdb.api;

import at.ac.fhcampuswien.fhmdb.models.Genre;

public class Builder {


    private final StringBuilder urlBuilder;
/*

    private MovieAPIRequest(StringBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    public static class Builder {
        private StringBuilder urlBuilder;
*/

        public Builder() {
            this.urlBuilder = new StringBuilder("https://prog2.fh-campuswien.ac.at/movies");
        }


        public Builder query(String query) {
            if (query != null && !query.isEmpty()) {
                appendDelimiter();
                urlBuilder.append("query=").append(query);
                // https://prog2.fh-campuswien.ac.at/movies?query=userinput&genre=ACTION
            }
            return this;
        }

        public Builder genre(Genre genre) {
            if (genre != null) {
                appendDelimiter();
                urlBuilder.append("genre=").append(genre);
            }
            return this;
        }

        public Builder releaseYear(String releaseYear) {
            if (releaseYear != null) {
                appendDelimiter();
                urlBuilder.append("releaseYear=").append(releaseYear);
            }
            return this;
        }

        public Builder ratingFrom(String ratingFrom) {
            if (ratingFrom != null) {
                appendDelimiter();
                urlBuilder.append("ratingFrom=").append(ratingFrom);
            }
            return this;
        }

        private void appendDelimiter() {

            if (urlBuilder.indexOf("?") == -1) {
                urlBuilder.append("?");
                // https://prog2.fh-campuswien.ac.at/movies?query=userinput&genre=ACTION
            } else {
                {
                    urlBuilder.append("&");
                }
            }
        }

    public String getUrl() {
        return urlBuilder.toString();
    }
}
