package com.whoisconnected.tools;


public class PortStatus{
    private int portNo;
    private boolean isOpen;

    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}