package pl.edu.agh.ki.sr.injuries;

public enum Injury {
    hip("hip"),
    knee("knee"),
    elbow("elbow");

    private final String name;

    Injury(String name) {
        this.name = name;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
