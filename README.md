# Loop-imdb-graph
Project work done with an image taken from the Internet Movie Database for the sake of solidifying graph theory to practical representation and implementation.

### Execution
The program can be run by navigating to the "Objektgraf"-folder and compiling "Graf.java", then running it. The program takes no explicit commandline parameters, so the command `java Graf` is enough after compiling

### Dataset, design and size
The dataset consists of two main .tsv-files; movies.tsv and actors.tsv. These files contain unique tt-ids for each movie and actor included in the image taken from the full IMDb database.
The files store the data in a table where rows are split by newlines and cells are split by a '\t' and ending with an empty newline. There are a total of 107898 movies and 126196 actors

Consequentially, this results in a graph with 234094 nodes and roughly 775000 edges as the graph is implemented with movies and actors each counting as a separate node. This design choice was made to efficiently store data in the proposed edges; both actors and movies are edges between their counterpart. The graph is modeled utilizing two neighbour lists, aEdges and mEdges, as the program frequently checks a node's neighbours through different algorithms.

### The program
At startup, the program will read the movies.tsv and actors.tsv files in that order then construct the graph as a Graf-object. Following this, the graph's size will be printed to `stdout` and a sequence begins running the following three program flows:
- Six degrees of IMDb
- Dijknchill
- Count all graph components

#### Six degrees of IMDb
This part of the program is inspired by the infamous *Six Degrees of Separation*-theory and simulates the degrees of separation between two given actors. The program will read the names or tt-ids of two actors of the user's choice from `stdin`, then try to find the shortest possible path between them based on the graph's dataset. Output will be generated and printed to `stdout` as follows:  
  Actor 1  
  ===[ Movie 1 (6.6) ] ===> Intermediate actor  
  ===[ Movie 2 (8.9) ] ===> Actor 2  

#### Dijknchill
While the name might not suggest so, the premise of this part of the program is to find the **chillest** path from actor 1 to actor 2 (taken from input like in Six degrees of IMDb). The chillest path will only include the highest rated movies such that the overall "weight" of the path is as short as possible. This might mean the path will include several movies of a higher rating as opposed to only a few with worse ratings.  
  Actor 1  
  ===[ Movie 1 (9.7) ] ===> Intermediate actor  
  ===[ Movie 2 (9.2) ] ===>  Intermediate actor  
  ===[ Movie 3 (8.9) ] ===>  Actor 2  
  Total weight: (10 - 9.7) + (10 - 9.2) + (10 - 8.9) = 1.2  
  Here we can observe the chillest path going through three movies and two intermediate actors, while the shortest path only goes through two movies and a single intermediate actor.

#### Graph component analysis
The last flow in the program gives a count of all the graph's components and their size. This allows us to see the structure and clustering in the graph, albeit not graphically. Output is formatted as:  
There are *X* components of size *x*
