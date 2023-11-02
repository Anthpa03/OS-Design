public class SystemClock {
    private int currentTime;
    public SystemClock()
    {
        currentTime = 0;
    }
    public int getCurrentTime()
    {
        return currentTime;
    }
    public void tick()
    {
        currentTime++;
    }
}
