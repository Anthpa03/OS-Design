public class Job {
    private String programName;
    private int arrivalTime;
    private int jobID;
    private int state;
    private static int jobIDCounter = 1;
    private int acc;
    private int sdr;
    private int psiar;
    private int csiar;
    private int tmpr;
    private int sar;
    private int[] ir;
    private int mir;

    // Constructor
    public Job(String programName, int arrivalTime)
    {
        this.programName = programName;
        this.arrivalTime = arrivalTime;
        this.jobID = getNewJobID();
        this.state = 0;
    }

    // Getters and setters
    public String getProgramName()
    {
        return programName;
    }

    public int getArrivalTime()
    {
        return arrivalTime;
    }
    public int getJobID()
    {
        return jobID;
    }

    public void setJobID(int jobID)
    {
        this.jobID = jobID;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    private static int getNewJobID()
    {
        return jobIDCounter++;
    }
    public int getAcc() {
        return acc;
    }

    public void setAcc(int acc) {
        this.acc = acc;
    }

    public int getCsiar() {
        return csiar;
    }

    public void setCsiar(int csiar) {
        this.csiar = csiar;
    }

    public int getMir() {
        return mir;
    }

    public void setMir(int mir) {
        this.mir = mir;
    }

    public int getPsiar() {
        return psiar;
    }

    public void setPsiar(int psiar) {
        this.psiar = psiar;
    }

    public int getSdr() {
        return sdr;
    }

    public void setSdr(int sdr) {
        this.sdr = sdr;
    }

    public int getTmpr() {
        return tmpr;
    }

    public void setTmpr(int tmpr) {
        this.tmpr = tmpr;
    }

    public int[] getIr() {
        return ir;
    }

    public void setIr(int[] ir) {
        this.ir = ir;
    }

    public int getSar() {
        return sar;
    }

    public void setSar(int sar) {
        this.sar = sar;
    }
}
