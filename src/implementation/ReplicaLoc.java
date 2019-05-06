package implementation;

import java.io.Serializable;

public class ReplicaLoc implements Serializable {


    private String address;
    private int id;
    private boolean alive;

    public ReplicaLoc(int id, String address, boolean alive) {
        this.id = id;
        this.address = address;
        this.alive = alive;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
