package net.werpu.tools.supportive.dtos;

public class Offset {
    int start;
    int end;

    public Offset(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}