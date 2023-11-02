public class Memory {
    int[][] memory;
    private boolean[] allocated;
    public Memory(int size)
    {
        allocated = new boolean[size];
        for (int i = 0; i < size; i++)
        {
            allocated[i] = false;
        }
        memory = new int[size][2];
    }
    public int allocate(int size)
    {
        for (int i = 0; i < memory.length - size; i++)
        {
            if (!allocated[i] && isContiguousMemory(i, size))
            {
                allocateRegion(i, size);
                return i;
            }
        }
        return -1; // No free space of specified size found
    }
    private boolean isContiguousMemory(int start, int size)
    {
        for (int i = start; i < start + size; i++)
        {
            if (allocated[i])
            {
                return false;
            }
        }
        return true;
    }
    private void allocateRegion(int start, int size)
    {
        for (int i = start; i < start + size; i++)
        {
            allocated[i] = true;
        }
    }
    public int[] read(int address)
    {
        int[] instruction = {memory[address][0], memory[address][1]};
        return instruction;
    }
    public int getOperand(int address)
    {
        return memory[address][1];
    }

    public void write(int add, int opcode, int operand)
    {
        memory[add][0] = opcode;
        memory[add][1] = operand;
    }

    public boolean writeToMem(int address, int data)
    {
        memory[address][0] = 0;
        memory[address][1] = data;
        return true;
    }
    public void clear()
    {
        for (int[] cell : memory)
        {
            cell[1] = 0;
        }
    }
    public void displayMemoryContents() {
        System.out.println("Memory Contents:");
        for (int i = 0; i < memory.length; i++) {
            int[] instruction = memory[i];
            int address = i;
            int opcode = instruction[0];
            int operand = instruction[1];
            System.out.println("[" + address + "] | " + opcode + " " + operand);
        }
    }
}
