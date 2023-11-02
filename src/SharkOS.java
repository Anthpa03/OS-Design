import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SharkOS
{
    int ACC; int SDR; int PSIAR; int CSIAR; int TMPR; int SAR; int[] IR; int MIR;
    private Job currentJob;
    SystemClock clock = new SystemClock();
    static Queue<Job> jobQueue = new LinkedList<>();
    Memory MEMORY;
    private final int TIME_QUANTUM = 4; // Time Quantum for round-robin scheduling
    private boolean yield = false;
    public void SharkOS()
    {
        INIT_SYSTEM();
        LOAD_SHARKOS_PROGRAMS();
        RUN_SHARKOS();
        EXIT_SHARKOS();
    }
    public void INIT_SYSTEM()
    {
        // Initializing registers
        ACC = 0;
        SDR = 0;
        PSIAR = 0;
        CSIAR = 0;
        TMPR = 0;
        SAR = 0;
        IR = new int[2];
        MIR = 0;

        // Initializing memory
        MEMORY = new Memory(1024);
    }

    private void LOAD_SHARKOS_PROGRAMS() {
        List<Job> jobs = createJobOrder(); // Determines order of jobs
        loadInstructions(jobs); // Stores each instruction into memory
    }
    private List<Job> createJobOrder() {
        // Define the path to the arrival times file & store them in a list
        String filename = "src/arrivalTimes.txt";
        List<Job> jobs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            // Read each line from the arrival times file and split them to create a new job
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                String programName = parts[0];
                int arrivalTime = Integer.parseInt(parts[1]);
                Job job = new Job(programName, arrivalTime);
                jobs.add(job);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Now, sort the jobs in order of arrival time
        jobs.sort(Comparator.comparingInt(Job::getArrivalTime));
        return jobs;
    }

    private void loadInstructions(List<Job> jobs) {
        File programDirectory = new File("src/programFiles");
        // Processes jobs in order of arrival time
        for (Job job : jobs) {
            jobQueue.add(job); // Adds the job to the job queue
            String programName = job.getProgramName();
            File programFile = new File(programDirectory + "/" + programName);
            if (programFile.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(Path.of(programFile.getPath()));
                    // Determine the program size (number of instructions) and allocate a space in MEMORY for it
                    int programSize = lines.size();
                    int allocatedAddress = MEMORY.allocate(programSize);
                    job.setPsiar(allocatedAddress);

                    // Checks if there is sufficient memory for the program to be stored and if there is,
                    // then it loads the program instructions into the allocated memory region
                    if (allocatedAddress != -1) {
                        for (int i = 0; i < programSize; i++) {
                            String[] parts = lines.get(i).split(" ");
                            int opcode = opcodeConversion(parts[0]);
                            int operand = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
                            MEMORY.write(allocatedAddress + i, opcode, operand);
                        }
                        System.out.println("Program " + programFile.getName() + " loaded successfully.");
                    } else {
                        System.out.println("Insufficient memory for program: " + programFile.getName());
                        System.exit(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int opcodeConversion(String opcode)
    {
        return switch (opcode) {
            case "LDI" -> 0;
            case "ADD" -> 10;
            case "SUB" -> 20;
            case "LDA" -> 30;
            case "STR" -> 40;
            case "BRH" -> 50;
            case "CBR" -> 60;
            case "HALT" -> 70;
            case "YLD" -> 80;
            default -> {
                System.exit(0);
                yield -1;
            }
        };
    }

    private void RUN_SHARKOS()
    {
        while (!jobQueue.isEmpty())
        {
                currentJob = jobQueue.poll();
                System.out.println("Now running Job ID " + currentJob.getJobID());
                currentJob.setState(1);
                loadState(currentJob);
                while (currentJob.getState() != 0)
                {
                    // Performs fetch decode execute and then prints all registers
                    fetchInstruction();
                    executeInstruction();
                    printRegisters();
                    clock.tick();
                    // Ticks when an instruction is executed

                    if (currentJob.getState() == 0)
                    {
                        System.out.println("End of Job ID " + currentJob.getJobID() + "\n");
                        break;
                    }
                    // Check if time quantum has expired
                    if (clock.getCurrentTime() % TIME_QUANTUM == 0 || yield)
                    {
                        contextSwitch();
                        // Sets yield to false if it was set to true
                        if(yield)
                        {
                            yield = false;
                        }
                        break;
                    }
                }
        }
    }


    private void contextSwitch()
    {
        // Save the current job's state
        saveState(currentJob);
        System.out.println("Context Switch occurring to Job ID " + currentJob.getJobID());
        // Add the job back to the queue for future execution
        jobQueue.offer(currentJob);
    }

    public void loadState(Job job)
    {
        ACC = job.getAcc();
        CSIAR = job.getCsiar();
        MIR = job.getMir();
        PSIAR = job.getPsiar();
        SAR = job.getSar();
        SDR = job.getSdr();
        TMPR = job.getTmpr();
        IR = job.getIr();
    }

    public void saveState(Job job)
    {
        job.setAcc(ACC);
        job.setCsiar(CSIAR);
        job.setMir(MIR);
        job.setPsiar(PSIAR);
        job.setSar(SAR);
        job.setSdr(SDR);
        job.setTmpr(TMPR);
        job.setIr(IR);
    }

    public void fetchInstruction()
    {
        SAR = PSIAR;
        IR = MEMORY.read(SAR);
        CSIAR = IR[0];
        SDR = IR[1];
    }
    private void executeInstruction()
    {
        switch (CSIAR)
        {
            case 0:
                LDI();
                break;
            case 10:
                ADD();
                break;
            case 20:
                SUB();
                break;
            case 30:
                LDA();
                break;
            case 40:
                STR();
                break;
            case 50:
                BRH();
                break;
            case 60:
                CBR();
                break;
            case 70:
                HALT();
                break;
            case 80:
                YLD();
                break;
        }
    }

    private void printRegisters()
    {
        System.out.println("Registers: ");
        System.out.println("ACC = " + ACC);
        System.out.println("PSIAR = " + PSIAR);
        System.out.println("SAR = " + SAR);
        System.out.println("SDR = " + SDR);
        System.out.println("TMPR = " + TMPR);
        System.out.println("CSIAR = " + CSIAR);
        System.out.println("IR = " + IR[0] + "  " + IR[1]);
        System.out.println("MIR = " + MIR);
        System.out.println();
    }
    public void ADD()
    {
        TMPR = ACC;
        ACC = PSIAR + 1;
        PSIAR = ACC;
        ACC = TMPR;
        TMPR = SDR;
        SAR = TMPR;
        SDR = MEMORY.getOperand(SAR);
        TMPR = SDR;
        ACC = ACC + TMPR;
        CSIAR = 0;
    }

    public void SUB() {
        TMPR = ACC;
        ACC = PSIAR + 1;
        PSIAR = ACC;
        ACC = TMPR;
        TMPR = SDR;
        SAR = TMPR;
        SDR = MEMORY.getOperand(SAR);
        TMPR = SDR;
        ACC -= TMPR;
        CSIAR = 0;
    }

    public void STR()
    {
        TMPR = ACC;
        ACC = PSIAR + 1;
        PSIAR = ACC;
        ACC = TMPR;
        TMPR = SDR;
        SAR = TMPR;
        SDR = ACC;
        MEMORY.writeToMem(SAR,SDR);
        CSIAR = 0;
    }
    public void LDI()
    {
        ACC = PSIAR + 1;
        PSIAR = ACC;
        ACC = SDR;
        CSIAR = 0;
    }

    public void LDA()
    {
        ACC = PSIAR + 1;
        PSIAR = ACC;
        TMPR = SDR;
        SAR = TMPR;
        SDR = MEMORY.getOperand(SAR);
        ACC = SDR;
        CSIAR = 0;
    }
    public void BRH()
    {
        PSIAR += SDR;
        CSIAR = 0;
    }
    public void CBR()
    {
        if (ACC == 0)
        {
            PSIAR += SDR;
        } else
        {
            TMPR = ACC;
            ACC = PSIAR + 1;
            PSIAR = ACC;
            ACC = TMPR;
        }
        CSIAR = 0;
    }
    public void YLD()
    {
        TMPR = ACC;
        ACC = PSIAR + 1;
        PSIAR = ACC;
        ACC = TMPR;
        yield = true;
    }
    public void HALT()
    {
        currentJob.setState(0);
    }
    public void EXIT_SHARKOS()
    {
        MEMORY.displayMemoryContents(); // Just showing the memory before clearing to make sure things are correct
        ACC = 0;
        SDR = 0;
        PSIAR = 0;
        CSIAR = 0;
        TMPR = 0;
        SAR = 0;
        IR = new int[2];
        MIR = 0;
        // Clear out memory
        MEMORY.clear();
    }
}