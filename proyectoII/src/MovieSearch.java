import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieSearch extends JFrame {

    private static final String API_KEY = "eba71768ad1290ad4dc8d305cef81e0a";
    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";

    private final List<MovieInfo> movieInfoList = new ArrayList<>();
    private int currentPage = 1;
    private boolean isSearching = false;
    private String lastSearchTerm = "";

    public MovieSearch() {
        setTitle("Movie Search");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JPanel resultPanel = new JPanel(new GridLayout(0, 1));
        JScrollPane scrollPane = new JScrollPane(resultPanel);

        JPanel topPanel = new JPanel();
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(prevButton);
        topPanel.add(nextButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        searchButton.addActionListener(e -> {
            currentPage = 1;
            String searchTerm = searchField.getText();
            if (!searchTerm.isEmpty()) {
                isSearching = true;
                lastSearchTerm = searchTerm;
                searchMovies(searchTerm, resultPanel);
            } else {
                JOptionPane.showMessageDialog(MovieSearch.this, "Please enter a"
                        + " search term", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadMovies(resultPanel);
            } else if (isSearching) {
                isSearching = false;
                loadMovies(resultPanel);
            }
        });

        nextButton.addActionListener(e -> {
            currentPage++;
            loadMovies(resultPanel);
        });
        loadMovies(resultPanel);
    }

    private void loadMovies(JPanel resultPanel) {
        try {
            String apiUrl;
            if (isSearching) {
                apiUrl = BASE_URL + "?api_key=" + API_KEY + "&query=" +
                        lastSearchTerm + "&page=" + currentPage;
            } else {
                apiUrl = "https://api.themoviedb.org/3/movie/popular?api_key=" 
                        + API_KEY + "&page=" + currentPage;
            }

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader =
            new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString())
                    .getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            resultPanel.removeAll();
            movieInfoList.clear();

            for (int i = 0; i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                JsonElement titleElement = movie.get("title");
                JsonElement posterPathElement = movie.get("poster_path");
                JsonElement idElement = movie.get("id");

                if (titleElement != null && !titleElement.isJsonNull() &&
                        posterPathElement != null && 
                        !posterPathElement.isJsonNull() 
                        && idElement != null && !idElement.isJsonNull()) {
                    String title = titleElement.getAsString();
                    String posterPath = posterPathElement.getAsString();
                    String imageUrl = "https://image.tmdb.org/t/p/w500" + 
                            posterPath;

                    JLabel movieLabel = createMovieLabel(title, imageUrl);
                    resultPanel.add(movieLabel);

                    movieInfoList.add(new MovieInfo(title, imageUrl, 
                            idElement.getAsInt()));
                }
            }

            resultPanel.revalidate();
            resultPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading movies", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchMovies(String searchTerm, JPanel resultPanel) {
        try {
            String apiUrl = BASE_URL + "?api_key=" + API_KEY + "&query=" + 
                    searchTerm + "&page=" + currentPage;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = 
             new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString())
                    .getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            resultPanel.removeAll();
            movieInfoList.clear();

            for (int i = 0; results != null && i < results.size(); i++) {
                JsonObject movie = results.get(i).getAsJsonObject();
                JsonElement titleElement = movie.get("title");
                JsonElement posterPathElement = movie.get("poster_path");
                JsonElement idElement = movie.get("id");

                if (titleElement != null && !titleElement.isJsonNull() && posterPathElement
                        != null && !posterPathElement.isJsonNull() && idElement 
                        != null && !idElement.isJsonNull()) {
                    String title = titleElement.getAsString();
                    String posterPath = posterPathElement.getAsString();
                    String imageUrl = "https://image.tmdb.org/t/p/w500" +
                            posterPath;

                    JLabel movieLabel = createMovieLabel(title, imageUrl);
                    resultPanel.add(movieLabel);

                    movieInfoList.add(new MovieInfo(title, imageUrl, 
                            idElement.getAsInt()));
                }
            }

            resultPanel.revalidate();
            resultPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching for movies", 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createMovieLabel(String title, String imageUrl) {
        JLabel movieLabel = new JLabel(title);

        new Thread(() -> {
            try {
                URL imageURL = new URL(imageUrl);
                BufferedImage img = ImageIO.read(imageURL);

                int width = 150;
                int height = 225;
                Image scaledImg = img.getScaledInstance(width, height, 
                        Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImg);

                SwingUtilities.invokeLater(() -> {
                    movieLabel.setIcon(icon);
                    movieLabel.revalidate();
                    movieLabel.repaint();
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();

        movieLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int movieId = movieInfoList.stream()
                        .filter(movieInfo -> movieInfo.getTitle().equals(title))
                        .findFirst()
                        .map(MovieInfo::getMovieId)
                        .orElse(-1);
                showMovieInformation(title, movieId);
            }
        });

        return movieLabel;
    }

    private void showMovieInformation(String title, int movieId) {
        try {
            String apiUrl = "https://api.themoviedb.org/3/movie/" + movieId +
                   "?api_key=" + API_KEY + "&append_to_response=credits,videos";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = 
             new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject movieDetails = JsonParser.parseString(response.toString())
                    .getAsJsonObject();

            String director = extractDirector(movieDetails);
            String synopsis = movieDetails.get("overview").getAsString();
            List<String> genres = extractGenres(movieDetails);
            String trailerLink = extractTrailerLink(movieDetails);
            List<String> actors = extractActors(movieDetails);

            StringBuilder infoMessage = new StringBuilder();
            infoMessage.append("Title: ").append(title).append("\n");
            infoMessage.append("Director: ").append(director).append("\n");
            infoMessage.append("Actors: ").append(String.join(", ", actors)).append("\n");
            infoMessage.append("Synopsis: \n").append(synopsis).append("\n");
            infoMessage.append("Genres: ").append(String.join(", ", genres)).append("\n");

            JButton closeButton = new JButton("Close");
            JButton trailerButton = new JButton("Watch Trailer");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            buttonPanel.add(trailerButton);

            JPanel dialogPanel = new JPanel(new BorderLayout());
            JTextArea synopsisTextArea = new JTextArea(infoMessage.toString());
            synopsisTextArea.setLineWrap(true);
            synopsisTextArea.setWrapStyleWord(true);
            synopsisTextArea.setEditable(false);
            synopsisTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
            dialogPanel.add(new JScrollPane(synopsisTextArea), BorderLayout.CENTER);
            dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

            JDialog movieDialog = new JDialog(this, "Movie Information", true);
            movieDialog.getContentPane().add(dialogPanel);
            movieDialog.setSize(400, 300);
            movieDialog.setLocationRelativeTo(this);

            closeButton.addActionListener(e -> movieDialog.dispose());

            trailerButton.addActionListener(e -> {
                if (!trailerLink.isEmpty()) {
                    try {
                        Desktop.getDesktop().browse(new URL(trailerLink).toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            movieDialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving movie details",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String extractDirector(JsonObject movieDetails) {
        JsonObject credits = movieDetails.getAsJsonObject("credits");
        for (JsonElement crewElement : credits.getAsJsonArray("crew")) {
            JsonObject crew = crewElement.getAsJsonObject();
            if ("Director".equals(crew.get("job").getAsString())) {
                return crew.get("name").getAsString();
            }
        }
        return "N/A";
    }

    private List<String> extractGenres(JsonObject movieDetails) {
        JsonArray genres = movieDetails.getAsJsonArray("genres");
        List<String> genreList = new ArrayList<>();

        for (JsonElement genreElement : genres) {
            JsonObject genreObject = genreElement.getAsJsonObject();
            JsonElement nameElement = genreObject.get("name");

            if (nameElement != null && !nameElement.isJsonNull()) {
                genreList.add(nameElement.getAsString());
            }
        }

        return genreList;
    }

    private String extractTrailerLink(JsonObject movieDetails) {
        JsonObject videos = movieDetails.getAsJsonObject("videos");
        for (JsonElement videoElement : videos.getAsJsonArray("results")) {
            JsonObject video = videoElement.getAsJsonObject();
            if ("Trailer".equals(video.get("type").getAsString())) {
                return "https://www.youtube.com/watch?v=" + video.get("key")
                        .getAsString();
            }
        }
        return "";
    }

    private List<String> extractActors(JsonObject movieDetails) {
        JsonObject credits = movieDetails.getAsJsonObject("credits");
        JsonArray cast = credits.getAsJsonArray("cast");
        List<String> actors = new ArrayList<>();

        for (JsonElement castElement : cast) {
            JsonObject actor = castElement.getAsJsonObject();
            JsonElement nameElement = actor.get("name");

            if (nameElement != null && !nameElement.isJsonNull()) {
                actors.add(nameElement.getAsString());
            }
        }

        return actors;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MovieSearch frame = new MovieSearch();
            frame.setVisible(true);
        });
    }
}

class MovieInfo {
    private String title;
    private String imageUrl;
    private int movieId;

    public MovieInfo(String title, String imageUrl, int movieId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getMovieId() {
        return movieId;
    }
}










