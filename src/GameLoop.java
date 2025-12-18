import simulation.Universe;

public class GameLoop {

    private boolean running = true;
    private double tickRate = 60.0; // ticks per second
    private Universe universe;
    private GUIManager gui;

    public GameLoop()
    {
        this.universe = new Universe();
        this.gui = new GUIManager(universe);
    }

    public void start()
    {
        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / tickRate;

        while (running)
        {
            long now = System.nanoTime();
            double delta = (now - lastTime) / nsPerTick;
            lastTime = now;

            update(delta / tickRate);
            render();
        }
    }
    private void update(double dt)
    {
        universe.update(dt);
        //gui.update(dt);
    }
    private void render()
    {
        gui.render();
    }
}
