package net.novaproject.novauhc.world;

public class BorderPhase {

    private final int id;
    private final int timer;
    private final double size;
    private final Double centerX;
    private final Double centerZ;
    private boolean executed = false;

    public BorderPhase(int id, int timer, double size, Double centerX, Double centerZ) {
        this.id = id;
        this.timer = timer;
        this.size = size;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public int getId() {
        return id;
    }

    public int getTimer() {
        return timer;
    }

    public double getSize() {
        return size;
    }

    public boolean hasCenter() {
        return centerX != null && centerZ != null;
    }

    public Double getCenterX() {
        return centerX;
    }

    public Double getCenterZ() {
        return centerZ;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}

