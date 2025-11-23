class Actor extends Node implements Comparable <Actor>{
    double priority;

    public Actor(String i, String n) {
        super(i, n);
    }

    public Actor setprio(double n){
        priority = n;
        return this;
    }
    @Override
    public int compareTo(Actor comp){
        return Double.compare(this.priority, comp.priority);
    }
    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return id;
    }
}