import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

//stopper Resource leak: 'reader' is never closed
@SuppressWarnings("resource")

class Graf {
    //instansvariablers
    //Lager to hashmaps basert på hvilken node som er "starten" til en kant
    //for lettere oversikt og oppslag
    HashMap<Actor, Set<Movie>> aEdges = new HashMap<>();
    HashMap<Movie, Set<Actor>> mEdges = new HashMap<>();
    
    public static void main(String[] args) {
        //deklarerer filbaner som konstanter for enklere oversikt
        final String GRAPH_MOVIES = "inputs/movies.tsv";
        final String GRAPH_ACTORS = "inputs/actors.tsv";
        
        //instansierer oversikt over film-id til Movie-objekt og Graf-objekt
        HashMap<String, Movie> movies = new HashMap<>();
        Graf graf = new Graf();
        
        //trycatch for å fange IO- og NumberFormat-feil
        try {
            BufferedReader reader = new BufferedReader(new FileReader(GRAPH_MOVIES));
            String line;
            
            //leser inn linjene i fil med alle filmer og legger inn i oversikten 'movies'
            while ((line = reader.readLine())!= null) {
                String[] inputs = line.split("\t");
                Movie movie = new Movie(inputs[0], inputs[1], Double.parseDouble(inputs[2]), Integer.parseInt(inputs[3]));
                movies.put(movie.id, movie);
                //legger inn tomt HashSet her for å forhindre overskrivelse av verdier
                graf.mEdges.put(movie, new HashSet<>());
            }
            
            //rerouter inputStream og leser inn linjer fra fil med alle skuespillere
            reader = new BufferedReader(new FileReader(GRAPH_ACTORS));
            while ((line = reader.readLine())!= null) {
                String[] inputs = line.split("\t");
                
                Actor actor = new Actor(inputs[0], inputs[1]);
                graf.aEdges.put(actor, new HashSet<>());
                
                //Passer på at alle felt i inputlinje etter navn og id blir behandlet som filmer
                //Og legger inn info i aEdges og mEdges slik at en kant går begge veier
                //I koden så langt har vi antatt at alle skuespillere har spilt i minst én film
                for(int x = 2; x < inputs.length; x++) {
                    //slår opp i film-oversikt basert på film-id hentet fra input
                    Movie tempMovie = movies.get(inputs[x]);
                    
                    //oppretter felt og legger inn i kant-hashmapene
                    if (tempMovie != null) {
                        graf.aEdges.get(actor).add(tempMovie);
                        graf.mEdges.get(tempMovie).add(actor);
                    }
                }
            }
        } catch (Exception e) {
            //printer feilbane
            e.printStackTrace();
        }
        //print total amount of nodes and edges
        System.out.println("\nGraph succesfully generated.");
        System.out.println(graf.countNodesEdges());

        //make a looping UI for the user to choose functionality
        Scanner flowIO = new Scanner(System.in);
        String flowChoice = "";
        while (!flowChoice.equals("exit")) {
            //print flow choices for user
            System.out.println(
                "\nPlease choose one of the following options:\n" +
                "1: Six degrees of IMDb - Find the shortest path between two actors\n" +
                "2: Dijknchill - Find the 'chillest' path between two actors\n" +
                "3: Count components - Give information about the graph's components and their size\n" +
                "exit: Exit the program and terminate.\n"
            );
            System.out.print("Choice: ");
            flowChoice = flowIO.nextLine();

            //handle behaviour based on choice
            switch (flowChoice) {
                case "1":
                    System.out.println("\nSix degrees of IMDb:");
                    System.out.println(graf.sixDegrees());
                    break;
                case "2":
                    System.out.println("\nDijknChill");
                    System.out.println(graf.dijknchill());
                    break;
                case "3":
                    System.out.println("\nGraph components:");
                    System.out.println(graf.findComponents());
                    break;
                case "exit": 
                    System.out.println("\nExiting program...\n");
                    break;
                default:
                    System.out.println("\nUnknown choice... please choose from one of the following choices:\n");
            }
        }
    }
    
    private String countNodesEdges() {
        //teller antall noder ved å se på størrelse til hashmap, hver nøkkel er en node
        //bruker både movies- og actors-map fordi begge er noder i vår struktur
        int nodes = aEdges.size() + mEdges.size();
        int edges = 0;
        
        //looper gjennom HashMapene og adderer størrelsen på Set-et som inneholder alle koblinger
        //til variabelen edges
        for (Actor key : aEdges.keySet()) {
            edges += aEdges.get(key).size();
        }
        
        return String.format("Antall noder: %d\nAntall kanter: %d", nodes, edges);
    }

    //TODO: Unify input-mechanism for fetching Actors from user + standardify input
    
    /** Dette er metoden brukt for oppgaven sixDegreesOfIMDB
     * @return En streng som pent representerer veien fra 'from' til 'to' 
     */
    private String sixDegrees() {
        Scanner scan = new Scanner(System.in);
        
        System.out.print("fra (id eller navn): ");
        Actor from = findActor(scan.nextLine());
        
        System.out.print("til (id eller navn): ");
        Actor to = findActor(scan.nextLine());
        
        return shortestRoad(from, to);
    }

    /** 
     * Henter skuespilleren med en ID eller navn lik argumentet.
     * Returnerer null om skuespilleren ikke er funnet
     * @param actorId En streng som er lik IDen eller navnet til en actor i aEdges
     */
    private Actor findActor(String actorId) {
        List<Actor> actorsFound = new ArrayList<>();

        //add all actors matching search to actorsFound list
        for (Actor actor: aEdges.keySet()) {
            if (actorId.equals(actor.getId()) || actorId.equals(actor.toString())) {
                actorsFound.add(actor);
            }
        }
        
        //handle behaviour based on the amount of actors matching the search
        if (actorsFound.size() < 1) {
            return null;
        } else if (actorsFound.size() == 1) {
            return actorsFound.getFirst();
        } else { //actorsFound.size() > 1 => Let user choose between IDs            
            return chooseBetweenMultipleActors(actorsFound);
        }
    }

    /**
     * Lets the user pick a single choice from all actors found by the findActor procedure
     * @param actorsFound - a List<Actor> containing all actors found by findActor
     * @return the Actor object of the actor chosen by the user
     */
    private Actor chooseBetweenMultipleActors(List<Actor> actorsFound) {
        //print choices
        System.out.println("\nMultiple actors found. Please choose:\n");
        for (int i = 0; i < actorsFound.size(); i++) {
            System.out.println(String.format("%d: %s (%s)", i+1, actorsFound.get(i).toString(), actorsFound.get(i).id));
        }

        //handle user input - catch NumberFormatException
        Scanner actorIO = new Scanner(System.in);
        Actor actorChoice = null;

        //retry input if user makes invalid choice
        while (actorChoice == null) {
            System.out.print("Choice: ");
            String actorInput = actorIO.nextLine(); 
            int actorIndex; //buffer actorIndex outside try_catch
            try {
                //try parsing input to integer
                actorIndex = Integer.parseInt(actorInput);

                //throw NumberFormatException if input outside index range
                if (actorIndex-1 >= actorsFound.size()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                //print error message and let user try again
                System.out.println("Invalid choice. Please choose an integer number between 1 and " + actorsFound.size());
                continue;
            }
            //end loop once valid choice is made
            actorChoice = actorsFound.get(actorIndex-1);
        }
        return actorChoice;
    }
    
    /** Finner og returnerer en korteste vei (som en streng) fra argument1 til argument2 
     * @param from Et objekt av klassen Actor (som er i aEdges)
     * @param to Et objekt av klassen Actor (som er i aEdges)
    */
    public String shortestRoad(Actor from, Actor to) {
        HashMap<Actor, Node[]> allPaths = breadthFirstSearch(from);
        ArrayList<Node> fastestPath = new ArrayList<>();
        
        // Returnerer en beskjed om det ikke finnes en sti mellom 'to' og 'front'
        if (!allPaths.containsKey(to)) {
            return String.format("Fant ikke en felles sti mellom %s og %s", from, to);
        }
        
        //TODO: Standardize and utilize byggvei method for path creation to reduce duplicated code
        // Lager et array som viser veien fra 'to' til 'from'
        Node current = to;
        while (current != from) {
            // System.err.println(current);
            fastestPath.add(current);
            current = allPaths.get(current)[1];
        }

        // Bygger strengen som representerer veien fra 'from' til 'to'
        StringBuilder stringPath = new StringBuilder(from + "\n");
        for (int i = fastestPath.size()-1; i >= 0; i--) {
            stringPath.append(String.format("===[ %s ] ===> %s\n", allPaths.get(fastestPath.get(i))[0].toString(), fastestPath.get(i)));
        }

        return stringPath.toString();
    }

    /** Bredde-først søk som går fra 'from' 
     * @param from Et objekt av klassen Actor (som er i aEdges)
     * @return Et HashMap (fra Actor til Node[]) hvor index 1 i Node[] er skuespilleren som la nøkkelen inn i hashmappet, og index 0 er filmen de begge har spilt i
    */
    private HashMap<Actor, Node[]> breadthFirstSearch(Actor from) {
        // Dette er dequen vi må iterere gjennom
        Deque<Actor> deque = new LinkedList<>();
        deque.addLast(from);

        // Alle noder vi har besøkt
        Set<Actor> visited = new HashSet<>();

        HashMap<Actor, Node[]> result = new HashMap<>(); // Index 0 i arrayet er filmen, index 1 er parent. Dette er dritstygt, men jeg kommer ikke på noe bedre
        result.put(from, null);

        while (deque.size() != 0) {
            Actor current = deque.removeFirst();
            // System.out.println(current);
            
            for (Movie movie: aEdges.get(current)) {
                for (Actor actor: mEdges.get(movie)) {
                    
                    if (!visited.contains(actor)) {
                        visited.add(actor);
                        deque.addLast(actor);

                        result.put(actor, new Node[]{movie, current}); // Legger til nye actor, dens parent, og hvilken film den deler med parent
                    }
                }
            }
        }
        return result;
    }

    /**
     * Metoden brukt for Chilleste vei
     * @return String med den veien med lavest rating fra en skuespiller til en annen
     */
    public String dijknchill(){
        //oppretter hashmapp som sier hvor langt unna start man er
        HashMap<Actor,Double> distanse = new HashMap<>();
        //lagrer koblingen to skuespillere og filmen som kobler de. Hvor Node[0] er filmen og Node[1] er skuespilleren kanten går til
        HashMap<Node, Node[]> kant = new HashMap<>();

        //spør og henter aktuelle skuespillere
        Scanner scan = new Scanner(System.in);
        System.out.print("fra (id eller navn): ");
        Actor from = findActor(scan.nextLine());
        System.out.print("til (id eller navn): ");
        Actor goal = findActor(scan.nextLine());

        //Setter start avstanden til alle noder til uendelig og uten kanter
        for (Actor actor : aEdges.keySet()) {
            distanse.put(actor, Double.POSITIVE_INFINITY);
            kant.put(actor,new Node[2]);
        }
        //setter start avstand til 0
        distanse.put(from, 0.0);
        PriorityQueue<Actor> queue = new PriorityQueue<>();

        //startnnoden har prioritet 0 
        from.setprio(0.0);
        //legger til startnoden i køen
        queue.add(from);
        
        while(!queue.isEmpty()){
            //fjerner den skuespilleren med høyest prioritet og setter den som den nåverenede skuespilleren
            Actor current = queue.poll();

            if(current.equals(goal)){
                break;
            }
            //går gjennom alle filmer og skuespillere som den nåverende skuespillern er koblet sammen med
            for(Movie movie: aEdges.get(current)){
                for(Actor actor : mEdges.get(movie)){
                    //finner den nye distansen til alle skuespillerne som er koblet sammen med den nåverenede skuespillern
                    double nydist = distanse.get(current) +(10- movie.weight);
                    //sjekker om den nye avstanden er kortere enn den som finnes
                    if(nydist < distanse.get(actor)){
                        //oppdaterer prioritet
                        actor.setprio(nydist);
                        distanse.put(actor, nydist);
                        //lagrer veien til noden
                        kant.put(actor, new Node[]{movie,current});
                        //fjerner skuespilleren for at prioriteten skal bli oppdatert
                        queue.remove(actor);
                        queue.add(actor);
                    }
                }
            }
            
        }
        return byggvei(from, goal, kant);
    }
    
    /**
     * Rekonstruer stien dijkstras algorytme finner
     * @param from Noden stien starter fra
     * @param goal Noden hvor stien stopper
     * @param kant Hashmap med en skuespiller som Key. Value-en er et array med to elementer hvor det første er en film key skuespilleren 
     * har spilt i, og det andre elementet er en skuespiller som også har spilt i filmen
     * @return String med stien formatert
     */
    private String byggvei(Actor from, Actor goal, HashMap<Node,Node[]> kant) {
        ArrayList<Node> vei = new ArrayList<>();
        Node current = goal;

        //Legger stien til i lista fra målet til starten
        while (current != from) {
            vei.add(current);
            current = kant.get(current)[1];
        } 
        vei.add(from); //legger til startnoden
        

        Collections.reverse(vei); //reverserer listen slik at den er fra start til slutt
        StringBuilder res = new StringBuilder();
        double totalvekt = 0.0;
        res.append(from+ "\n");
        
        for (int i = 1; i < vei.size(); i++) {
            Node skuespiller = vei.get(i);

            Node film = kant.get(skuespiller)[0];
            totalvekt += (10- film.getWeight());
            res.append(String.format("===[ %s ] ===> %s\n",film , skuespiller));
        }
        res.append("TOTAL VEKT:"+totalvekt);
        return res.toString();
    }
    
    /** Finner alle komponenter i grafen, og returnerer en String som beskriver hvor mange komponenter det finnes av forskjellige størrelser 
     * @return Strengen som beskriver hvor mange komponenter det finnes av forskjellige størrelser
    */
    private String findComponents() {
        // Teller antallet ganger en gitt lengde skjer
        HashMap<Integer, Integer> occurrences = new HashMap<>();

        // Bruker denne for å sjekke om en actor allerede er i en komponent
        HashSet<Node> unionOfComponents = new HashSet<>();

        for (Actor actor: aEdges.keySet()) {
            if (!unionOfComponents.contains(actor)) {

                HashSet<Node> current = depthFirstSearch(actor);
                unionOfComponents.addAll(current);

                if (occurrences.get(current.size()) == null) {
                    occurrences.put(current.size(), 1);
                }
                else {
                    occurrences.put(current.size(), occurrences.get(current.size())+1);
                }
            }
        }

        //sørger for at alle filmer som ikke har skuespillere også kommer med i komponentoversikten i likhet med skuespillere som ikke er i noen filmer.
        for (Movie movie: mEdges.keySet()) {
            if (mEdges.get(movie).size() == 0) {
                occurrences.put(1, occurrences.get(1)+1);            
            }
        }

        return createComponentString(occurrences);
    }

    /** Finner alle skuespillere og filmer 'from' har en sti med
     * @param from Et Actor-objekt i aEdges
     * @return Et HashSet med alle skuespillere og filmer som deler en sti med 'from' ('from' inkludert)
    */
    private HashSet<Node> depthFirstSearch(Actor from) {
        HashSet<Node> result = new HashSet<>();
        result.add(from);

        HashSet<Actor> visited = new HashSet<>();
        visited.add(from);

        // FIFO-listen vi itererer gjennom
        Deque<Actor> queue = new LinkedList<>();
        queue.offer(from);

        while (!queue.isEmpty()) {
            Actor current = queue.poll();

            // Finner alle skuespillere current har en kant med, og legger de i visited og queue
            for (Movie movie: aEdges.get(current)) {
                result.add(movie);
                for (Actor actor: mEdges.get(movie)) {
                    if (!visited.contains(actor)) {
                        visited.add(actor);
                        result.add(actor);
                        queue.offer(actor);
                    }
                }
            }
        }
        return result;
    }

    /** Gitt et HashMap som beskriver hvor vanlig hver størrelse er, returnerer denne informasjonen som en lesbar streng 
     * @param occurrences HashMap fra Integer til Integer, hvor key er en vilkårlig lengde, og value er antallet ganger den lengden ble funnet
     * @return informasjonen i map-et som en lesbar streng 
    */
    private String createComponentString(HashMap<Integer, Integer> occurrences) {
        StringBuilder string = new StringBuilder();

        for (Integer length: occurrences.keySet()) {
            string.append(String.format("There are %s components of size %s\n", occurrences.get(length), length));
        }
        return string.toString();
    }
}