class Movie extends Node{
    //instansvariabler
    double weight;
    int votes;

    public Movie(String i, String n, double w, int stemmer) {
        super(i, n);
        weight = w;
        votes = stemmer;
    }
    public Double getWeight(){
        return weight;
    }
    @Override
    public String toString() {
        return String.format("%s (%s)", name, weight);
    }
}